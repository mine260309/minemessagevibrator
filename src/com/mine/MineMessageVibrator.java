package com.mine;

import android.content.Context;
import android.os.Vibrator;
import android.telephony.TelephonyManager;


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

	public static void notifyReminder(Context context) {
		MineLog.v("notifying reminder");
		vibrate(context);
	}
	
	private static void vibrate(Context context) {
	    // Get phone state, if not idle then don't vibrate
	    TelephonyManager mTM = 
	      (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	    if (mTM.getCallState()== TelephonyManager.CALL_STATE_IDLE) {
	    	Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	    	v.vibrate(1000);
	    }
	}
}
