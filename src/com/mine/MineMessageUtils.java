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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
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
		getGmailAccount(context);
		Cursor c = context.getContentResolver().query(
				Uri.withAppendedPath(Gmail.LABELS_URI, GmailAccount), LABEL_PROJECTION, 
				null, null, null);
		if (c != null) {
			try {
				while (c.moveToNext()) {
					String canonicalName = c.getString(0);
					if (UNSEEN.equals(canonicalName)) {
						ret = c.getInt(1);						
					}
				}
			} finally {
				c.close();
			}
		}
		MineLog.v("Gmail account: secret, getUnreadGmails: " + ret);
		return ret;
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
