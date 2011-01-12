package com.mine;

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
	
	/**
	 * Return the number of missed phone calls
	 * 
	 * @param context
	 * @return missed phone calls
	 */
	synchronized public static int getUnreadGmails(Context context) {
		int ret = 0;
		GmailAccount = "mine260309@gmail.com";
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
		MineLog.v("getUnreadGmails: " + ret);
		return ret;
	}

    private static final class Gmail {
    	public static final String AUTHORITY = "gmail-ls";
	    public static final String AUTHORITY_PLUS_LABELS = "content://" + AUTHORITY + "/labels/";
	    public static final Uri LABELS_URI = Uri.parse(AUTHORITY_PLUS_LABELS);			
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
    private static String GmailAccount;
}
