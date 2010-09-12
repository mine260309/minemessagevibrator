package com.mine;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class MineMessageUtils {

	private static final String UNREAD_CONDITION = "read=0";
	public static final String SMSMMS_ID = "_id";
	
	public static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
	public static final Uri SMS_INBOX_CONTENT_URI = Uri.withAppendedPath(SMS_CONTENT_URI, "inbox");
	public static final Uri MMS_CONTENT_URI = Uri.parse("content://mms");
	public static final Uri MMS_INBOX_CONTENT_URI = Uri.withAppendedPath(MMS_CONTENT_URI, "inbox");	


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
	      SMS_INBOX_CONTENT_URI,
	      projection,
	      selection,
	      selectionArgs,
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
	      MMS_INBOX_CONTENT_URI,
	      projection,
	      selection, null, null);

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
}
