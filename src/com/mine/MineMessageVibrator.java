package com.mine;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;


public class MineMessageVibrator {
//	private static final int MINE_MESSAGE_NOTIFICATION_ID = 0x1086;
//	private static Notification smsNotify = null;
//	private static Notification mmsNotify = null;
	public static final int VIBRATE_REASON_SMS = 0;
	public static final int VIBRATE_REASON_MMS = VIBRATE_REASON_SMS;
	public static final int VIBRATE_REASON_REMINDER = 2;

	private static final String VIBRATE_MODE_SHORT = "Short";
	private static final String VIBRATE_MODE_MIDDLE = "Middle";
	private static final String VIBRATE_MODE_LONG = "Long";
	private static final String VIBRATE_MODE_MULTIPLE_SHORT = "Multiple Short";
	private static final String VIBRATE_MODE_MULTIPLE_MIDDLE = "Multiple Middle";
	private static final String VIBRATE_MODE_SAME_AS_MESSAGE = "Same as Message";
	private static final String VIBRATE_MODE_CUSTOM = "Custom";

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

	private static void vibrate(Context context, int reason) {
		// If the phone is in silent mode, do not vibrate
		if(MineVibrationToggler.ShallNotify(context)) {
		    // Get phone state, if not idle then don't vibrate
		    TelephonyManager mTM = 
		      (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		    if (mTM.getCallState()== TelephonyManager.CALL_STATE_IDLE) {
		    	//Check whether we need to ring or vibrate, or both...
		    	boolean needSound = (reason == VIBRATE_REASON_REMINDER) &&
		    				MineVibrationToggler.GetReminderSoundEnabled(context);
		    	boolean needVibrate = MineVibrationToggler.GetVibrationEnabled(context);
//		    	MineLog.v("VibrationEnabled is " + MineVibrationToggler.GetVibrationEnabled(context)
//		    			  + "\n ReminderVibrateEnabled is " +  MineVibrationToggler.GetReminderVibrateEnabled(context));
		    	if (needSound) {
		    		NotificationManager nm = 
		    			(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		    		Notification n = new Notification();
		    		
		    	    // Try and parse the user preference, use the default if it fails
		    	    Uri reminderSoundURI =
		    	      Uri.parse(MineVibrationToggler.GetReminderSoundString(context));
		    	    
		    		n.sound = reminderSoundURI;
		    		nm.notify(reason, n);
		    		MineLog.v("Playing sound " + reminderSoundURI);
	    		}

		    	if (needVibrate) {
		    		Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		    		v.vibrate(GetVibratePattern(context, reason), -1);
		    		MineLog.v("Vibrating... ");
		    	}
		    }
		    else {
		    	MineLog.v("phone in call, not vibrate");
		    }
		}
		else {
			MineLog.v("phone in silent mode, not vibrate");
		}
	}

	private static long[] GetVibratePattern(Context context, int reason){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String pattern;
		long[] pat = null;
		if (reason == VIBRATE_REASON_REMINDER) {
			pattern = settings.getString(context.getString(R.string.pref_reminder_vibration_mode_key), "Same as Message");
			if (pattern.equals(VIBRATE_MODE_SAME_AS_MESSAGE)) {
				reason = VIBRATE_REASON_SMS;
			}
			else if (pattern.equals(VIBRATE_MODE_SHORT)) {
				pat = VibratePatternShort;
				MineLog.v("Reminder Vibrate using pattern "+pattern);
			}
			else if (pattern.equals(VIBRATE_MODE_MIDDLE)) {
				pat = VibratePatternMiddle;
				MineLog.v("Reminder Vibrate using pattern "+pattern);
			}
			else if (pattern.equals(VIBRATE_MODE_LONG)) {
				pat = VibratePatternLong;
				MineLog.v("Reminder Vibrate using pattern "+pattern);
			}
			else if (pattern.equals(VIBRATE_MODE_MULTIPLE_SHORT)) {
				pat = VibratePatternMultipleShort;
				MineLog.v("Reminder Vibrate using pattern "+pattern);
			}
			else if (pattern.equals(VIBRATE_MODE_MULTIPLE_MIDDLE)) {
				pat = VibratePatternMultipleMiddle;
				MineLog.v("Reminder Vibrate using pattern "+pattern);
			}
			else if (pattern.equals(VIBRATE_MODE_CUSTOM)) {
				pat = MineVibrationToggler.parseVibratePattern(
						settings.getString(context.getString(R.string.pref_reminder_vibrate_pattern_key),
								context.getString(R.string.pref_vibrate_pattern_default)));
				if (pat == null) {
					MineLog.e("Parse Custom Pattern Error, using defalt Middle");
					pat = VibratePatternMiddle;
				}
				MineLog.v("Vibrate using custom pattern " + pat.toString());
			}
			else {
				//default, use same as message
				reason = VIBRATE_REASON_SMS;
			}
		}
		if (reason != VIBRATE_REASON_REMINDER) {
			// If it's SMS or MMS vibration
			pattern = settings.getString(context.getString(R.string.pref_vibration_mode_key), "Middle");
			if(pattern.equals(VIBRATE_MODE_SHORT)) {
				pat = VibratePatternShort;
				MineLog.v("Vibrate using pattern "+pattern);
			}
			else if (pattern.equals(VIBRATE_MODE_MIDDLE)) {
				pat = VibratePatternMiddle;
				MineLog.v("Vibrate using pattern "+pattern);
			}
			else if (pattern.equals(VIBRATE_MODE_LONG)) {
				pat = VibratePatternLong;
				MineLog.v("Vibrate using pattern "+pattern);
			}
			else if (pattern.equals(VIBRATE_MODE_MULTIPLE_SHORT)) {
				pat = VibratePatternMultipleShort;
				MineLog.v("Vibrate using pattern "+pattern);
			}
			else if (pattern.equals(VIBRATE_MODE_MULTIPLE_MIDDLE)) {
				pat = VibratePatternMultipleMiddle;
				MineLog.v("Vibrate using pattern "+pattern);
			}
			else if (pattern.equals(VIBRATE_MODE_CUSTOM)) {
				pat = MineVibrationToggler.parseVibratePattern(
						settings.getString(context.getString(R.string.pref_sms_vibrate_pattern_key),
								context.getString(R.string.pref_vibrate_pattern_default)));
				if (pat == null) {
					MineLog.e("Parse Custom Pattern Error, using defalt Middle");
					pat = VibratePatternMiddle;
				}
				MineLog.v("Vibrate using custom pattern " + pat.toString());
			}
			else {
				//default, use middle
				pat = VibratePatternMiddle;
				MineLog.v("Vibrate using pattern "+"default");
			}
		}
		if (pat == null) {
			// This shall never happen...
			// But let's use default middle
			MineLog.v("Vibration is ERROR! Use middle");
			pat = VibratePatternMiddle;
		}
		return pat;
	}
}
