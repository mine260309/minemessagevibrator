package com.mine;

import android.content.Context;
import android.os.Vibrator;


public class MineMessageVibrator {
//	private static final int MINE_MESSAGE_NOTIFICATION_ID = 0x1086;
//	private static Notification smsNotify = null;
//	private static Notification mmsNotify = null;
	
	public static void notifySMS(Context context) {
		MineLog.v("notifying SMS");
		vibrate(context);

		//TODO: maybe I can use notification instead
	}
	
	public static void notifyMMS(Context context) {
		MineLog.v("notifying MMS");
		vibrate(context);

		//TODO: maybe I can use notification instead
	}

	private static void vibrate(Context context) {
		Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(1000);
	}
}
