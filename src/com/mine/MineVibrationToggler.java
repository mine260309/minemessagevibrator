package com.mine;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.provider.Settings;

public class MineVibrationToggler {

	public static final String VIBRATION_ACTION_NAME = "com.mine.MESSAGE_VIBRATION_CHANGED";
	
//	private static final String PREFS_NAME = "MineMessageVibrationPrefFile";
	private static boolean VibrateEnabled;
	private static boolean ReminderEnabled;
	private static boolean ReminderVibrateEnabled;
	private static boolean ReminderSoundEnabled;
	private static boolean inited = false;
	
	private static void initStatus(Context context) {
		if (!inited) {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			VibrateEnabled = settings.getBoolean(
					context.getString(R.string.pref_vibrate_enable_key), false);		
			ReminderEnabled = settings.getBoolean(
					context.getString(R.string.pref_reminder_enable_key), false);
			ReminderVibrateEnabled = settings.getBoolean(
					context.getString(R.string.pref_reminder_vibrate_enable_key), false);
			ReminderSoundEnabled = settings.getBoolean(
					context.getString(R.string.pref_reminder_sound_enable_key), false);
			inited = true;
		}
	}

	public static void EnableMessageVibration(Context context, boolean enable) {
		initStatus(context);
		PackageManager pm = context.getPackageManager();
		ComponentName cn = new ComponentName(context, MineMessageReceiver.class);
		int enable_disable = enable? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
				:PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		pm.setComponentEnabledSetting(cn, 
				enable_disable,
				PackageManager.DONT_KILL_APP);

		if(!enable) {
			MineMessageReminderReceiver.cancelReminder(context);
		}

		SetVibrationEnable(context, enable);
	}
	
	public static void EnableReminderVibrate(Context context, boolean enable) {
		initStatus(context);
		ReminderVibrateEnabled = enable;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(context.getString(R.string.pref_reminder_vibrate_enable_key), enable);

		// Commit the edits!
		editor.commit();
	}
	
	public static boolean GetReminderEnabled(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		ReminderEnabled = settings.getBoolean(
				context.getString(R.string.pref_reminder_enable_key), false);
		return ReminderEnabled;
	}
	
	public static boolean GetVibrationEnabled(Context context) {
		initStatus(context);
		return VibrateEnabled;
	}
	
	public static boolean GetReminderVibrateEnabled(Context context) {
		initStatus(context);
		return ReminderVibrateEnabled;
	}
	
	public static boolean GetReminderSoundEnabled(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		ReminderSoundEnabled = settings.getBoolean(
				context.getString(R.string.pref_reminder_sound_enable_key), false);
		return ReminderSoundEnabled;
	}
	
	public static int GetReminderInterval(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String minutes = settings.getString(context.getString(R.string.pref_reminder_interval_key), "5");
		int seconds = 60 * Integer.valueOf(minutes);
		return seconds;
	}

	public static String GetReminderSoundString(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String reminder = settings.getString(
				context.getString(R.string.pref_reminder_sound_key), 
				Settings.System.DEFAULT_NOTIFICATION_URI.toString());
		return reminder;
	}

	public static boolean IsFirstRun(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		boolean ret = settings.getBoolean(context.getString(R.string.pref_first_time_run_key), true);
		if (ret) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(context.getString(R.string.pref_first_time_run_key), false);
			editor.commit();
		}
		return ret;
	}

	public static boolean IsUpgraded(Context context) {
		PackageManager manager = context.getPackageManager();
		int nowVersion = 0;
		try {
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			nowVersion = info.versionCode;  //°æ±¾ºÅ
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		int storedVersion = GetVersion(context);
		if(nowVersion > storedVersion) {
			SetVersion(context, nowVersion);
			return true;
		}
		return false;
	}
	
	public static boolean IsWakeLockEnabled(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return  settings.getBoolean(
				context.getString(R.string.pref_reminder_wakelock_enable_key), false);
	}
	
	public static boolean ShallVibrate(Context context) {
		int notifyMode = GetPhoneRingerState(context);
		if (notifyMode ==  AudioManager.RINGER_MODE_SILENT) {
			MineLog.v("phone is in silent mode");
			return false;
		}
		MineLog.v("phone is in mode: " + notifyMode);
		return true;
	}

	/**
	 * Parse the user provided custom vibrate pattern into a long[]
	 *
	 */
	public static long[] parseVibratePattern(String stringPattern) {
	    ArrayList<Long> arrayListPattern = new ArrayList<Long>();
	    Long l;

	    if (stringPattern == null) return null;

	    String[] splitPattern = stringPattern.split(",");
	    int VIBRATE_PATTERN_MAX_SECONDS = 60000;
	    int VIBRATE_PATTERN_MAX_PATTERN = 100;

	    for (int i = 0; i < splitPattern.length; i++) {
	      try {
	        l = Long.parseLong(splitPattern[i].trim());
	      } catch (NumberFormatException e) {
	        return null;
	      }
	      if (l > VIBRATE_PATTERN_MAX_SECONDS) {
	        return null;
	      }
	      arrayListPattern.add(l);
	    }

	    // TODO: can i just cast the whole ArrayList into long[]?
	    int size = arrayListPattern.size();
	    if (size > 0 && size < VIBRATE_PATTERN_MAX_PATTERN) {
	      long[] pattern = new long[size];
	      for (int i = 0; i < pattern.length; i++) {
	        pattern[i] = arrayListPattern.get(i);
	      }
	      return pattern;
	    }

	    return null;
	}

	public static void SetVibratePatternbyReason(Context context, int reason, String pattern) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		String key;
		if(reason == MineMessageVibrator.VIBRATE_REASON_REMINDER)
			key = context.getString(R.string.pref_reminder_vibrate_pattern_key);
		else
			key = context.getString(R.string.pref_sms_vibrate_pattern_key);
		editor.putString(key, pattern);

		// Commit the edits!
		editor.commit();
	}
	
	public static String GetVibratePatternbyReason(Context context, int reason) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String key;
		if(reason == MineMessageVibrator.VIBRATE_REASON_REMINDER)
			key = context.getString(R.string.pref_reminder_vibrate_pattern_key);
		else
			key = context.getString(R.string.pref_sms_vibrate_pattern_key);
		return settings.getString(key, 
				context.getString(R.string.pref_vibrate_pattern_default));
	}

	public static int GetVersion(Context context){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getInt(context.getString(R.string.pref_mine_message_vibrator_version_key),
				0);
	}
	public static void SetVersion(Context context, int version){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(context.getString(R.string.pref_mine_message_vibrator_version_key), version);
		editor.commit();
	}
	
	private static int GetPhoneRingerState(Context context) {
		AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		return am.getRingerMode();
	}
	
	private static void SetVibrationEnable(Context context, boolean enable) {
      // We need an Editor object to make preference changes.
      // All objects are from android.context.Context
 //     SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		VibrateEnabled = enable;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(context.getString(R.string.pref_vibrate_enable_key), enable);

		// Commit the edits!
		editor.commit();
	}
}
