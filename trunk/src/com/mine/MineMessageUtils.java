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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
	 * @return missed phone calls
	 */
	synchronized public static int getUnreadGmails(Context context) {
		int ret = 0;
		String mailFeed = getGmailFeed(context);
/* for testing purpose
		if (!mailFeed.equals("")) {
			MineVibrationToggler.tempSaveFeedString(context, mailFeed);
		} else {
			mailFeed = MineVibrationToggler.tempLoadFeedString(context);
		}
*/
		Document feedDoc = XMLfromString(mailFeed);
		if (feedDoc != null) {
	        NodeList nodes = feedDoc.getElementsByTagName("entry");
	        ret = nodes.getLength();
	        MineLog.v("Parsed unread count: " + ret);
		} else {
			MineLog.e("Unable to parse the xml!");
		}

		MineLog.v("Gmail account: secret, getUnreadGmails: " + ret);
		return ret;
	}

	public static Document XMLfromString(String xml){
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

	private static String getGmailFeed(Context context) {
		String ret = "";
		String[] token = MineVibrationToggler.GetGmailToken(context);
		CommonsHttpOAuthConsumer consumer =
			new CommonsHttpOAuthConsumer("anonymous", "anonymous");
		consumer.setTokenWithSecret(token[0], token[1]);
		MineLog.v("consumer with token: " + token[0] + ", secret: " + token[1]);
		// create a request that requires authentication
        HttpGet request = new HttpGet("https://mail.google.com/mail/feed/atom/unread");

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
			MineLog.v("sending the request...");
			response = httpClient.execute(request);
			MineLog.v("after execute...");
			ret = read(response.getEntity().getContent());
			MineLog.v("response: " + ret);
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
    private static final class LabelColumns {    	
        public static final String CANONICAL_NAME = "canonicalName";
        public static final String NAME = "name";
        public static final String NUM_CONVERSATIONS = "numConversations";
        public static final String NUM_UNREAD_CONVERSATIONS = "numUnreadConversations";    
    }
    private static String[] LABEL_PROJECTION = {
        LabelColumns.CANONICAL_NAME,
        LabelColumns.NUM_UNREAD_CONVERSATIONS};
    private static final String UNSEEN = "^^unseen-^i";
    private static String GmailAccount = null;
}
