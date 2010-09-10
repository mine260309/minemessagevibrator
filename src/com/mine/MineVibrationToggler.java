package com.mine;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

public class MineVibrationToggler {

	public static final String VIBRATION_ACTION_NAME = "com.mine.MESSAGE_VIBRATION_CHANGED";
	
	private static final String PREFS_NAME = "MineMessageVibrationPrefFile";
	private static final String VIBRATION_SETTING = "vibrationMode";
//	public static boolean VibrationEnabled = false;

	public static void EnableMessageVibration(Context context, boolean enable) {
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
		
		SetVibrationMode(context, enable);
	}

	private static void SetVibrationMode(Context context, boolean enable) {
      // We need an Editor object to make preference changes.
      // All objects are from android.context.Context
      SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = settings.edit();
      editor.putBoolean(VIBRATION_SETTING, enable);

      // Commit the edits!
      editor.commit();
	}
	
	public static boolean GetVibrationMode(Context context) {
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return settings.getBoolean(VIBRATION_SETTING, false);
	}
}
