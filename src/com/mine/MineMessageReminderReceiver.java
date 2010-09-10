package com.mine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MineMessageReminderReceiver extends BroadcastReceiver {

	private static PendingIntent reminderPendingIntent = null;
	private static final int reminderInterval = 5*60; //reminder interval, in seconds
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		intent.setClass(context, MineMessageReminderService.class);
		MineMessageReminderService.beginStartingService(context, intent);
		
		MineLog.v("Receive an intent: "+ intent.getAction());
	}

	/**
	 * This will schedule a reminder notification to play in the future using the
	 * system AlarmManager. The time till the reminder and number of reminders is
	 * taken from user preferences.
	 * 
	 * @param context
	 * @param currentUnreadCount if -1 it will read unread count from inbox;
	 * if >0 it will use it as the message unread count;
	 * if ==0, it shall never happen!
	 */
	public static void scheduleReminder(Context context, int currentUnreadCount) {
		int unreadNumber;
		if (currentUnreadCount == -1) {
		    unreadNumber = MineMessageUtils.getUnreadMessagesCount(context);
		    // TODO: This is very hacky!! 
		    // It assumes this happens before the system add the current message 
		    unreadNumber++;
		    if (unreadNumber <= 0) {
		    	MineLog.e("scheduleReminder: get 0 unread message, why?!");
		    	return;
		    }
		}
		else if (currentUnreadCount == 0) {
			MineLog.e("scheduleReminder: currentUnreadCount is 0, why?!");
			return;
		}
		else {
			unreadNumber = currentUnreadCount;
		}
	    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	    Intent reminderIntent = new Intent(context, MineMessageReminderReceiver.class);
	    reminderIntent.setAction(MineMessageReminderService.ACTION_REMIND);
	    reminderIntent.putExtra(MineMessageReminderService.UNREAD_NUMBER, unreadNumber);

	    if (reminderPendingIntent != null) {
	    	cancelReminder(context);
	    }
	    reminderPendingIntent =
	        PendingIntent.getBroadcast(context, 0, reminderIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    
	    long triggerTime = System.currentTimeMillis() + (reminderInterval * 1000);
	    MineLog.v("MineMessageReminderReceiver: scheduled reminder notification in " 
	    		+ reminderInterval + " seconds, Unread: "+unreadNumber);
	    am.set(AlarmManager.RTC_WAKEUP, triggerTime, reminderPendingIntent);
	}

	/*
	 * Cancels the reminder notification in the case the user reads the message
	 * before it ends up playing.
	 */
	public static void cancelReminder(Context context) {
    	if (reminderPendingIntent != null) {
    		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    		am.cancel(reminderPendingIntent);
    		reminderPendingIntent.cancel();
    		reminderPendingIntent = null;
    		MineLog.v("MineMessageReminderReceiver: cancelReminder()");
    	}
	}
}
