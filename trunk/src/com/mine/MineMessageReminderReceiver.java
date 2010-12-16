package com.mine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class MineMessageReminderReceiver extends BroadcastReceiver {

	public static final int REMINDER_TYPE_WHATEVER = 0x00;
	public static final int REMINDER_TYPE_MESSAGE = 0x01;
	public static final int REMINDER_TYPE_PHONECALL = 0x02;
	
	private static PendingIntent reminderPendingIntent = null;
	// private static final int reminderInterval = 10;//5*60; //reminder
	// interval, in seconds
	private static PowerManager.WakeLock mStartingService;

	private static int reminderEnableState = 0;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		MineLog.v("Receive an intent: " + intent.getAction());
		intent.setClass(context, MineMessageReminderService.class);
		MineMessageReminderService.beginStartingService(context, intent);
	}

	/**
	 * This will schedule a reminder notification to play in the future using
	 * the system AlarmManager. The time till the reminder and number of
	 * reminders is taken from user preferences.
	 * 
	 * @param context
	 * @param currentUnreadCount
	 *            if -1 it will read unread count from inbox; if >0 it will use
	 *            it as the message unread count; if ==0, it shall never happen!
	 * @param type the reminder type, message or phone call 
	 */
	public static void scheduleReminder(Context context, int currentUnreadCount, int type) {

		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		// create the reminder intent
		Intent reminderIntent = new Intent(context,
				MineMessageReminderReceiver.class);
		reminderIntent.setAction(MineMessageReminderService.ACTION_REMIND);

		// calculate the trigger time
		int reminderIntervalSeconds = MineVibrationToggler
			.GetReminderInterval(context);
		long triggerTime = System.currentTimeMillis()
			+ (reminderIntervalSeconds * 1000);

		if (reminderPendingIntent != null) {
			cancelReminder(context, type);
		}

		if (type == REMINDER_TYPE_MESSAGE) {
			int unreadNumber = 0;
			if (currentUnreadCount == -1) {
	
				// Wait for a short while and then check the number of unread
				// messages;
				// This is intended to get the correct number
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				unreadNumber = MineMessageUtils.getUnreadMessagesCount(context);
				// TODO: This is very hacky!!
				// actually this is a bug!
				// It assumes this happens before the system add the current message
				// unreadNumber++;
				if (unreadNumber <= 0) {
					MineLog
							.e("scheduleReminder: get 0 unread message, maybe it's read?!");
					return;
				}
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
			reminderEnableState |= type;
		}
		else if (type == REMINDER_TYPE_PHONECALL) {
			// int missedPhonecall = 0;
			// should I add some extra information?
			MineLog
				.v("MineMessageReminderReceiver: scheduled reminder notification " +
					"for missed calls in "
					+ reminderIntervalSeconds
					+ " seconds");
			reminderEnableState |= type;
		}
		else if (type == REMINDER_TYPE_WHATEVER){
			MineLog
				.v("MineMessageReminderReceiver: scheduled reminder notification " +
				"for whatever in "
				+ reminderIntervalSeconds
				+ " seconds");
			reminderEnableState |= type;
		}
		else {
			MineLog.e("MineMessageReminderReceiver: invalid type!");
			return;
		}

		reminderIntent.putExtra(MineMessageReminderService.EXTRA_REMINDER_TYPE, type);
		
		reminderPendingIntent = PendingIntent.getBroadcast(context, 0,
				reminderIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		am.set(AlarmManager.RTC, triggerTime, reminderPendingIntent);
		acquireWakeLockIfNeeded(context);
	}

	/**
	 * Cancels the reminder notification in the case the user reads the message
	 * before it ends up playing.
	 * @param context
	 * @param type the reminder type
	 */
	public static void cancelReminder(Context context, int type) {
		if (reminderPendingIntent != null) {
			MineLog.v("cancelReminder() for type: " + type);

			if (type == REMINDER_TYPE_MESSAGE) {
				reminderEnableState &= (~type);
			}
			else if (type == REMINDER_TYPE_PHONECALL) {
				reminderEnableState &= (~type);
			}
			else if (type == REMINDER_TYPE_WHATEVER) {
				reminderEnableState &= type;
			}
			else {
				MineLog.e("cancelReminder: invalid type!");
				return;
			}
			if(reminderEnableState == REMINDER_TYPE_WHATEVER) {
				AlarmManager am = (AlarmManager) context
						.getSystemService(Context.ALARM_SERVICE);
				am.cancel(reminderPendingIntent);
				reminderPendingIntent.cancel();
				reminderPendingIntent = null;
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
}
