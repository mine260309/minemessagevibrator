/*************************************************************************
 * MineMessageVibrator is an Android App that provides vibrate and 
 * reminder functions for SMS, MMS, Gmail, etc. 
 * Copyright (C) 2010  Lei YU
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ************************************************************************/

package com.mine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.CallLog;

public class MineMessageUtils {

	private static final String UNREAD_CONDITION = "read=0";
	public static final String SMSMMS_ID = "_id";

	public static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
	public static final Uri SMS_INBOX_CONTENT_URI = Uri.withAppendedPath(
			SMS_CONTENT_URI, "inbox");
	public static final Uri MMS_CONTENT_URI = Uri.parse("content://mms");
	public static final Uri MMS_INBOX_CONTENT_URI = Uri.withAppendedPath(
			MMS_CONTENT_URI, "inbox");

	/**
	 * Return current unread message count from system db (sms and mms)
	 * 
	 * @param context
	 * @return unread sms+mms message count
	 */
	synchronized public static int getUnreadMessagesCount(Context context) {
		return getUnreadSmsCount(context) + getUnreadMmsCount(context);
	}

	/**
	 * Return current unread message count from system db (sms only)
	 * 
	 * @param context
	 * @return unread sms message count
	 */
	private static int getUnreadSmsCount(Context context) {

		final String[] projection = new String[] { SMSMMS_ID };
		final String selection = UNREAD_CONDITION;
		final String[] selectionArgs = null;
		final String sortOrder = null;

		int count = 0;

		Cursor cursor = context.getContentResolver().query(
				SMS_INBOX_CONTENT_URI, projection, selection, selectionArgs,
				sortOrder);

		if (cursor != null) {
			try {
				count = cursor.getCount();
			} finally {
				cursor.close();
			}
		}

		MineLog.v("sms unread count = " + count);
		return count;
	}

	/**
	 * Return current unread message count from system db (mms only)
	 * 
	 * @param context
	 * @return unread mms message count
	 */
	private static int getUnreadMmsCount(Context context) {

		final String selection = UNREAD_CONDITION;
		final String[] projection = new String[] { SMSMMS_ID };

		int count = 0;

		Cursor cursor = context.getContentResolver().query(
				MMS_INBOX_CONTENT_URI, projection, selection, null, null);

		if (cursor != null) {
			try {
				count = cursor.getCount();
			} finally {
				cursor.close();
			}
		}
		MineLog.v("mms unread count = " + count);
		return count;
	}
	
	/**
	 * Return the number of missed phone calls
	 * 
	 * @param context
	 * @return missed phone calls
	 */
	synchronized public static int getMissedPhoneCalls(Context context) {
		String queryString = CallLog.Calls.TYPE+"="+CallLog.Calls.MISSED_TYPE+" AND "+CallLog.Calls.NEW+"=1";;
		int ret = 0;
		Cursor c = context.getContentResolver().
			query(CallLog.Calls.CONTENT_URI, new String[]{"_id"}, 
					queryString, null, null);
		if (c != null) {
			try {
				if (c.moveToNext()) {
					ret= c.getCount();
				}
			} finally {
				c.close();
			}
		}
		MineLog.v("getMissedPhoneCalls: " + ret);
		return ret;
	}

	public static String getGmailAccount(Context context) {
		if (GmailAccount == null) {
			AccountManager am = AccountManager.get(context);
			Account[] accounts = am.getAccountsByType(Gmail.TYPE);
			if (accounts.length > 0) {
				GmailAccount = accounts[0].name;
			}
			else {
				MineLog.v("Unable to get gmail account!");
				GmailAccount = null;
			}
		}
		return GmailAccount;
	}
	
	/**
	 * Return the number of missed phone calls
	 * 
	 * @param context
	 * @return unread gmail count
	 */
	private static long lastCheckTime = 0;
	private static int lastCheckCount = 0;
	synchronized public static int getUnreadGmails(Context context) {
		int ret = 0;
		long timeSinceLastCheck = SystemClock.elapsedRealtime()-lastCheckTime;
		if (timeSinceLastCheck < 15000) {
			MineLog.v("check too frequnt, return saved count. interval: "
					+ timeSinceLastCheck);
			return lastCheckCount;
		}
		String mailFeed = getGmailFeed(context, null);
		lastCheckTime = SystemClock.elapsedRealtime();
/* for testing purpose
		if (!mailFeed.equals("")) {
			MineVibrationToggler.tempSaveFeedString(context, mailFeed);
		} else {
			mailFeed = MineVibrationToggler.tempLoadFeedString(context);
		}
*/
		Document feedDoc = XMLfromString(mailFeed);
		if (feedDoc != null) {
			if (verifyValid(mailFeed)) {
		        NodeList nodes = feedDoc.getElementsByTagName("entry");
		        ret = nodes.getLength();
		        MineLog.v("Parsed unread count: " + ret);
			}
			else {
				MineLog.v("Unauthorized feed!");
				handleUnauthorized(context);
			}
		} else {
			MineLog.e("Unable to parse the xml!");
		}

		MineLog.v("Gmail account: secret, getUnreadGmails: " + ret);
		lastCheckCount = ret;
		return ret;
	}
/*
	private static boolean verifyValid(Document doc) {
		NodeList nodes = doc.getElementsByTagName("title");
		return !( nodes.getLength() == 1 &&
				nodes.item(0).getTextContent().equals("Unauthorized") );
	}
*/
	private static boolean verifyValid(String doc) {
		return !(doc.startsWith("<HTML>") && doc.contains("<TITLE>Unauthorized"));
	}

	private static void handleUnauthorized(Context context) {
		// send notifiction of this
		MineVibrationToggler.invalidateGmailToken(context);
		showTokenInvalidNotification(context);
	}

	public static void showTokenInvalidNotification(Context context) {
		NotificationManager nm =
			(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

		CharSequence contentTitle = context.getText(R.string.token_invalid_notify_title);
		CharSequence contentText = context.getText(R.string.token_invalid_notify_text);

		Notification notification = new Notification(
				android.R.drawable.stat_notify_error,
				contentTitle, System.currentTimeMillis());

		Intent notificationIntent = new Intent(context, MineVibrationTabView.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		notification.defaults=Notification.DEFAULT_ALL; 
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		nm.notify(1, notification);
	}

	/**
	 * Verify gmail account with saved token
	 * It returns true only when 
	 * 1) System has a gmail account
	 * 2) Saved token is valid
	 * 3) The account related to the token matches System's account
	 */
	public static boolean verifyGmailAccountWithToken(
			Context context, String[] token)
				throws Exception {
		String systemAccount = getGmailAccount(context);
		if (systemAccount != null) {
			String mailFeed = getGmailFeed(context, token);
			Document feedDoc = XMLfromString(mailFeed);
			if (feedDoc != null) {
				NodeList nodes = feedDoc.getElementsByTagName("title");
				if (nodes.getLength() == 0) {
					MineLog.e("Unexpected title!");
				} else {
					String title = nodes.item(0).getTextContent();
					String tokenAccount = title.substring(title.lastIndexOf(' ')+1);
					MineLog.v("found account: " + tokenAccount);
					if (systemAccount.equals(tokenAccount)) {
						return true;
					}
					else {
						MineLog.e("Unmatch account! System: "
								+ systemAccount + ", token: "
								+tokenAccount);
						return false;
					}
				}
			} else {
				MineLog.e("Unable to parse the xml!");
			}
		}
		throw new Exception();
	}

	/** 
	 * Create a Document from XML using Xml parser.
	 * Copied from http://p-xr.com/android-tutorial-how-to-parseread-xml-data-into-android-listview/
	 * */
	private static Document XMLfromString(String xml){
	    Document doc = null;
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(xml));
	        doc = db.parse(is);
        } catch (ParserConfigurationException e) {
            MineLog.e("XML parse error: " + e.getMessage());
            return null;
        } catch (SAXException e) {
        	MineLog.e("Wrong XML file structure: " + e.getMessage());
            return null;
        } catch (IOException e) {
        	MineLog.e("I/O exeption: " + e.getMessage());
            return null;
        }
        return doc;
	}

	private static String getGmailFeed(Context context, String[] token) {
		String ret = "";
		if (token == null) {
			token = MineVibrationToggler.GetGmailToken(context);
		}
		CommonsHttpOAuthConsumer consumer =
			new CommonsHttpOAuthConsumer("anonymous", "anonymous");
		
		//!!! for testing purpose only, invalid the token
		/*
		char[] temp = token[0].toCharArray();
		temp[0] = 'i';
		token[0] = String.copyValueOf(temp);*/

		consumer.setTokenWithSecret(token[0], token[1]);

		// create a request that requires authentication
        HttpGet request = new HttpGet("https://mail.google.com/mail/feed/atom/");

        // sign the request
        try {
			consumer.sign(request);
		} catch (Exception e) {
			e.printStackTrace();
		}

        // send the request
        HttpClient httpClient = new DefaultHttpClient();
        org.apache.http.HttpResponse response;
		try {
			response = httpClient.execute(request);
			ret = read(response.getEntity().getContent());
			//MineLog.v("response: " + ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	private static String read(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
		for (String line = r.readLine(); line != null; line = r.readLine()) {
			sb.append(line);
		}
		in.close();
		return sb.toString();
	}

	public static String[] getGmailToken(Context context) {
		// Get the gmail token if exists
		// Start authenticate activity if not
		String[] token = MineVibrationToggler.GetGmailToken(context);
		if (token[0].equals("") || token[1].equals("")) {
			MineVibrationToggler.clearGmailTokenInvalid(context);
			context.startActivity(new Intent().setClass(context,
					com.mine.oauth.MineOAuthAccessActivity.class));
		}
		return token;
	}

    private static final class Gmail {
    	public static final String AUTHORITY = "gmail-ls";
	    public static final String AUTHORITY_PLUS_LABELS = "content://" + AUTHORITY + "/labels/";
	    public static final Uri LABELS_URI = Uri.parse(AUTHORITY_PLUS_LABELS);
	    public static final String TYPE = "com.google";
    }
    private static String GmailAccount = null;
}
