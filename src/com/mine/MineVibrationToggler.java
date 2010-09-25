package com.mine;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

	public static boolean ShallVibrate(Context context) {
		int notifyMode = GetPhoneRingerState(context);
		if (notifyMode ==  AudioManager.RINGER_MODE_SILENT) {
			MineLog.v("phone is in silent mode");
			return false;
		}
		MineLog.v("phone is in mode: " + notifyMode);
		return true;
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
