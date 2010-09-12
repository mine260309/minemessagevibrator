package com.mine;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;


public class MineMessageVibrator {
//	private static final int MINE_MESSAGE_NOTIFICATION_ID = 0x1086;
//	private static Notification smsNotify = null;
//	private static Notification mmsNotify = null;
	private static final int VIBRATE_REASON_SMS = 0;
	private static final int VIBRATE_REASON_MMS = VIBRATE_REASON_SMS;
	private static final int VIBRATE_REASON_REMINDER = 2;
	
	private static final long[] VibratePatternShort = new long[] {0, 500};
	private static final long[] VibratePatternMiddle = new long[] {0, 1200};
	private static final long[] VibratePatternLong = new long[] {0, 2000};
	private static final long[] VibratePatternMultipleShort = new long[] {0,500,200,500,200,500};
	private static final long[] VibratePatternMultipleMiddle = new long[] {0,1200,300,1200,300,1200};
	
	public static void notifySMS(Context context) {
		MineLog.v("notifying SMS");
		vibrate(context, VIBRATE_REASON_SMS);

		//TODO: maybe I can use notification instead
	}
	
	public static void notifyMMS(Context context) {
		MineLog.v("notifying MMS");
		vibrate(context, VIBRATE_REASON_MMS);

		//TODO: maybe I can use notification instead
	}

	public static void notifyReminder(Context context) {
		MineLog.v("notifying reminder");
		vibrate(context, VIBRATE_REASON_REMINDER);
	}
/*
	private static void vibrate(Context context) {
	    // Get phone state, if not idle then don't vibrate
	    TelephonyManager mTM = 
	      (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	    if (mTM.getCallState()== TelephonyManager.CALL_STATE_IDLE) {
	    	Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	    	v.vibrate(1000);
	    }
	}
*/
	private static void vibrate(Context context, int reason) {
	    // Get phone state, if not idle then don't vibrate
	    TelephonyManager mTM = 
	      (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	    if (mTM.getCallState()== TelephonyManager.CALL_STATE_IDLE) {
	    	Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	    	v.vibrate(GetVibratePattern(context, reason), -1);
	    }
	}
	
	private static long[] GetVibratePattern(Context context, int reason){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String pattern = settings.getString(context.getString(R.string.pref_vibration_mode_key), "Middle");
		long[] pat;
		if(pattern.equals("Short")) {
			pat = VibratePatternShort;
			MineLog.v("Vibrate using pattern "+pattern);
		}
		else if (pattern.equals("Middle")) {
			pat = VibratePatternMiddle;
			MineLog.v("Vibrate using pattern "+pattern);
		}
		else if (pattern.equals("Long")) {
			pat = VibratePatternLong;
			MineLog.v("Vibrate using pattern "+pattern);
		}
		else if (pattern.equals("Multiple Short")) {
			pat = VibratePatternMultipleShort;
			MineLog.v("Vibrate using pattern "+pattern);
		}
		else if (pattern.equals("Multiple Middle")) {
			pat = VibratePatternMultipleMiddle;
			MineLog.v("Vibrate using pattern "+pattern);
		}
		else {
			//default, use middle
			pat = VibratePatternMiddle;
			MineLog.v("Vibrate using pattern "+"default");
		}
		return pat;
	}
}
