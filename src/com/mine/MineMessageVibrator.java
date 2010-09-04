package com.mine;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Vibrator;


public class MineMessageVibrator {
	private static final int MINE_MESSAGE_NOTIFICATION_ID = 0x1086;
	private static Notification smsNotify = null;
	private static Notification mmsNotify = null;
	
	public static void notifySMS(Context context) {
		MineLog.v("notifying SMS");

//		createNotification();
//		NotificationManager nm =
//	        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

//		nm.notify(MINE_MESSAGE_NOTIFICATION_ID, smsNotify);
		vibrate(context);
	}
	
	public static void notifyMMS(Context context) {
		MineLog.v("notifying MMS");
		
//		createNotification();
//		NotificationManager nm =
//	        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

//		nm.notify(MINE_MESSAGE_NOTIFICATION_ID, smsNotify);
		vibrate(context);
	}
	
	private static void createNotification() {
		if (smsNotify == null) {
			smsNotify = new Notification();
			//smsNotify.defaults = Notification.DEFAULT_VIBRATE;
			smsNotify.vibrate = new long[] {0, 1000};
		}
		if (mmsNotify == null) {
			mmsNotify = smsNotify;
		}
	}
	private static void vibrate(Context context) {
		Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(1000);
	}
}
