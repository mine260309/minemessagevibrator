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

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

public class MineMessageReminderReceiver extends BroadcastReceiver {

	public static final int REMINDER_TYPE_NONE = 0x0;
	public static final int REMINDER_TYPE_MESSAGE = 0x01;
	public static final int REMINDER_TYPE_PHONECALL = 0x02;
	public static final int REMINDER_TYPE_GMAIL = 0x04;
	public static final int REMINDER_TYPE_WHATEVER = 0x7;
	private static final Object mReminderSync = new Object();

	private static PendingIntent reminderPendingIntent = null;
	// private static final int reminderInterval = 10;//5*60; //reminder
	// interval, in seconds
	private static PowerManager.WakeLock mStartingService;

	private static int reminderEnableState = 0;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		MineLog.v("Receive an intent: " + intent.getAction());
		intent.setClass(context, MineMessageReminderService.class);
		MineMessageReminderService.beginStartingService(context, intent);
	}

	/**
	 * This will schedule a reminder notification to play in the future using
	 * the system AlarmManager.
	 * A notification is sent to indicate what the reminder is for and can be
	 * cancelled by tap the notification.
	 * 
	 * @param context
	 * @param currentUnreadCount
	 *          For Message:
	 *            if -1 it will read unread count from inbox; if >0 it will use
	 *            it as the message unread count; if ==0, it shall never happen!
	 *          For phone call:
	 *            if -1 it is the first schedule, should not send notification
	 *            if 0 it is reminder's schedule, should send notification 
	 * @param type the reminder type, message or phone call or gmail
	 */
	public static void scheduleReminder(Context context, int currentUnreadCount, int type) {

		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		boolean shouldSendNotification = true;
		// create the reminder intent
		Intent reminderIntent = new Intent(context,
				MineMessageReminderReceiver.class);
		reminderIntent.setAction(MineMessageReminderService.ACTION_REMIND_SET);
		GetReminderType(context);
		
		// calculate the trigger time
		int reminderIntervalSeconds = MineVibrationToggler
			.GetReminderInterval(context);
		long triggerTime = System.currentTimeMillis()
			+ (reminderIntervalSeconds * 1000);

		if (type == REMINDER_TYPE_MESSAGE) {
			int unreadNumber = 0;
			if (currentUnreadCount == -1) {
	
				// Wait for a short while and then check the number of unread
				// messages;
				// This is intended to get the correct number
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				unreadNumber = MineMessageUtils.getUnreadMessagesCount(context);
				// It assumes this happens before the system add the current message
				// unreadNumber++;
				if (unreadNumber <= 0) {
					MineLog.e("scheduleReminder: get 0 unread message, maybe it's read?!");
					return;
				}
				shouldSendNotification = false;
			} else if (currentUnreadCount == 0) {
				MineLog.e("scheduleReminder: currentUnreadCount is 0, why?!");
				return;
			} else {
				unreadNumber = currentUnreadCount;
			}
			reminderIntent.putExtra(MineMessageReminderService.EXTRA_UNREAD_NUMBER,
					unreadNumber);
			MineLog
				.v("MineMessageReminderReceiver: scheduled reminder notification " +
						"for messages in "
					+ reminderIntervalSeconds
					+ " seconds, Unread: "
					+ unreadNumber);
			AddReminderType(context, type);
		}
		else if (type == REMINDER_TYPE_PHONECALL) {
			MineLog
				.v("MineMessageReminderReceiver: scheduled reminder notification " +
					"for missed calls in "
					+ reminderIntervalSeconds
					+ " seconds");
			AddReminderType(context, type);
			if (currentUnreadCount == -1) {
				// First time schedule for phone call, do not notify
				shouldSendNotification = false;
			}
		}
		else if (type == REMINDER_TYPE_GMAIL) {
			MineLog
			.v("MineMessageReminderReceiver: scheduled reminder notification " +
				"for unread gmails in "
				+ reminderIntervalSeconds
				+ " seconds");
			AddReminderType(context, type);
		}
		else if (type == REMINDER_TYPE_WHATEVER){
			MineLog
				.v("MineMessageReminderReceiver: scheduled reminder notification " +
				"for whatever in "
				+ reminderIntervalSeconds
				+ " seconds");
			AddReminderType(context, type);
		}
		else {
			MineLog.e("MineMessageReminderReceiver: invalid type!");
			return;
		}

		reminderIntent.putExtra(MineMessageReminderService.EXTRA_REMINDER_TYPE, reminderEnableState);

		synchronized (mReminderSync) {
			reminderPendingIntent = PendingIntent.getBroadcast(context, 0,
					reminderIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	
			am.set(AlarmManager.RTC, triggerTime, reminderPendingIntent);
		}
		
		if (shouldSendNotification) {
			sendNotification(context, type);
		}
		acquireWakeLockIfNeeded(context);
	}

	/**
	 * Cancels the reminder notification in the case the user reads the message
	 * before it ends up playing.
	 * @param context
	 * @param type the reminder type
	 */
	public static void cancelReminder(Context context, int type) {
		synchronized (mReminderSync) {
		if (reminderPendingIntent != null) {
			MineLog.v("cancelReminder() for type: " + type);

			if (type == REMINDER_TYPE_MESSAGE) {
				RemoveReinderType(context, type);
			}
			else if (type == REMINDER_TYPE_PHONECALL) {
				RemoveReinderType(context, type);
			}
			else if (type == REMINDER_TYPE_GMAIL) {
				RemoveReinderType(context, type);			
			}
			else if (type == REMINDER_TYPE_WHATEVER) {
				RemoveReinderType(context, type);
			}
			else {
				MineLog.e("cancelReminder: invalid type!");
				return;
			}
			if(reminderEnableState == REMINDER_TYPE_NONE) {
				AlarmManager am = (AlarmManager) context
						.getSystemService(Context.ALARM_SERVICE);
				am.cancel(reminderPendingIntent);
				reminderPendingIntent.cancel();
				reminderPendingIntent = null;
				cancelNotification(context);
				MineLog.v("MineMessageReminderReceiver: cancelReminder()");
				releaseWakeLock();
			}
			else if (type == REMINDER_TYPE_MESSAGE) {
				MineLog.v("NOT cancelReminder() for phone call");
			}
			else if (type == REMINDER_TYPE_PHONECALL) {
				MineLog.v("NOT cancelReminder() for message");
			}
			else {
				MineLog.e("cancelReminder() Error status!");
			}
		}
		}
	}

	private static void acquireWakeLockIfNeeded(Context context) {
		if (MineVibrationToggler.IsWakeLockEnabled(context)) {
			if (mStartingService == null) {
				PowerManager pm = (PowerManager) context
						.getSystemService(Context.POWER_SERVICE);
				mStartingService = pm.newWakeLock(
						PowerManager.PARTIAL_WAKE_LOCK, MineLog.LOGTAG
								+ ".MessageReminderService");
				mStartingService.setReferenceCounted(false);
			}
			mStartingService.acquire();
			MineLog.v("acquire wake lock");
		}
	}

	private static void releaseWakeLock() {
		if (mStartingService != null) {
			if (mStartingService.isHeld()) {
				mStartingService.release();
				MineLog.v("release wake lock");
			}
		}
	}
	
	private static void AddReminderType(Context c, int type) {
		reminderEnableState |= type;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		final Editor edit = prefs.edit();
		edit.putInt("mineReminderType", reminderEnableState);
		edit.commit();
		MineLog.v("MINEDBG: add reminder type " + type +", state " + reminderEnableState);
	}

	private static void RemoveReinderType(Context c, int type) {
		reminderEnableState &= (~type);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		final Editor edit = prefs.edit();
		edit.putInt("mineReminderType", reminderEnableState);
		edit.commit();
		MineLog.v("MINEDBG: remove reminder type " + type +", state " + reminderEnableState);
	}

	private static int GetReminderType(Context c) {
		if (reminderEnableState == 0) {
			// In case app gets killed, read it
			SharedPreferences settings = PreferenceManager
			.getDefaultSharedPreferences(c);
			reminderEnableState =  settings.getInt("mineReminderType", 0);
		}
		return reminderEnableState;
	}

	private static String getNotificationTitle(Context context, int type) {
		String notifyStrFormat = context.getString(R.string.notification_msg_title_format);
		String notifyItem; 
		switch(type) {
		case REMINDER_TYPE_MESSAGE:
			notifyItem = context.getString(R.string.notification_msg_sms);
			break;
		case REMINDER_TYPE_GMAIL:
			notifyItem = context.getString(R.string.notification_msg_gmail);
			break;
		case REMINDER_TYPE_PHONECALL:
			notifyItem = context.getString(R.string.notification_msg_missedphonecall);
			break;
		case REMINDER_TYPE_WHATEVER:
		default:
			notifyItem = "MINEDBG: I don't know what type...";
			break;
		}
		return String.format(notifyStrFormat, notifyItem);
	}

	private static final int NOTIFICATION_ID = 0x0309;
	private static void sendNotification(Context context, int type) {
		MineLog.v("send Notification");
		NotificationManager nm = (NotificationManager)
				context.getSystemService(Context.NOTIFICATION_SERVICE);
		// TODO: use proper icon
		Notification n = new Notification(R.drawable.icon, "",  System.currentTimeMillis());

		// Prepare notification string
		String notifyTitle = getNotificationTitle(context, type);
		String notifyInfo = context.getString(R.string.notification_msg_info);
		
		// Prepare notification
		RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification);
		contentView.setImageViewResource(R.id.notification_image, R.drawable.icon);
		contentView.setTextViewText(R.id.notification_title, notifyTitle);
		contentView.setTextViewText(R.id.notification_info, notifyInfo);
		n.contentView = contentView;
		Intent intent = new Intent(context, MineMessageReminderReceiver.class);
		intent.setAction(MineMessageReminderService.ACTION_REMIND_CANCEL);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				context, 0, intent, 0);
		n.contentIntent = pendingIntent;
		n.deleteIntent = pendingIntent;
		n.flags |= Notification.FLAG_AUTO_CANCEL;
		nm.notify(NOTIFICATION_ID, n);
	}
	
	private static void cancelNotification(Context context) {
		MineLog.v("cancel Notification");
		NotificationManager nm = (NotificationManager)
				context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(NOTIFICATION_ID);
	}
}
