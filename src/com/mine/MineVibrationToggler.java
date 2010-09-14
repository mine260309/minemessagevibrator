package com.mine;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

public class MineVibrationToggler {

	public static final String VIBRATION_ACTION_NAME = "com.mine.MESSAGE_VIBRATION_CHANGED";
	
//	private static final String PREFS_NAME = "MineMessageVibrationPrefFile";
	private static boolean VibrateEnabled;
	private static boolean ReminderEnabled;
	private static boolean inited = false;
	
	private static void initStatus(Context context) {
		if (!inited) {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			VibrateEnabled = settings.getBoolean(context.getString(R.string.pref_reminder_enable_key), false);		
			ReminderEnabled = settings.getBoolean(context.getString(R.string.pref_vibrate_enable_key), false);
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
	
	public static void EnableReminder(Context context, boolean enable) {
		initStatus(context);
		ReminderEnabled = enable;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(context.getString(R.string.pref_reminder_enable_key), enable);

		// Commit the edits!
		editor.commit();
	}
	
	public static boolean GetReminderEnabled(Context context) {
		initStatus(context);
		return ReminderEnabled;
	}
	
	public static boolean GetVibrationEnabled(Context context) {
		initStatus(context);
		return VibrateEnabled;
	}
	
	public static int GetReminderInterval(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String minutes = settings.getString(context.getString(R.string.pref_reminder_interval_key), "5");
		int seconds = 60 * Integer.valueOf(minutes);
		return seconds;
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
