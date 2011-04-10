/*************************************************************************
 * MineMessageVibrator is an Android App that provides vibrate and 
 * reminder functions for SMS, MMS, Gmail, etc. 
 * Copyright (C) 2010  Lei YU
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ************************************************************************/

package com.mine;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.provider.Settings;

public class MineVibrationToggler {

	public static final String VIBRATION_ACTION_NAME = "com.mine.MESSAGE_VIBRATION_CHANGED";

	// private static final String PREFS_NAME = "MineMessageVibrationPrefFile";
	private static boolean VibrateEnabled;
	private static boolean ReminderEnabled;
	// private static boolean ReminderVibrateEnabled;
	private static boolean ReminderSoundEnabled;
	private static boolean inited = false;

	private static void initStatus(Context context) {
		if (!inited) {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(context);
			VibrateEnabled = settings.getBoolean(context
					.getString(R.string.pref_vibrate_enable_key), false);
			ReminderEnabled = settings.getBoolean(context
					.getString(R.string.pref_reminder_enable_key), false);
			// ReminderVibrateEnabled = settings.getBoolean(
			// context.getString(R.string.pref_reminder_vibrate_enable_key),
			// false);
			ReminderSoundEnabled = settings.getBoolean(context
					.getString(R.string.pref_reminder_sound_enable_key), false);
			inited = true;
		}
	}

	/**
	 * Set the broadcast receiver for messages enable or disable; It depends on
	 * the Message Vibration checkbox & the reminder checkbox broadcast receiver
	 * will be disabled when both checkboxes are disabled
	 */
	private static void SetMessageReceiver(Context context) {
		PackageManager pm = context.getPackageManager();
		ComponentName cn = new ComponentName(context, MineMessageReceiver.class);
		boolean smsVibrateEnabled, reminderEnabled;
		smsVibrateEnabled = GetVibrationEnabled(context);
		reminderEnabled = GetReminderEnabled(context);
		// appAutoEnabled = GetAppAutoEnabled(context);

		boolean enable = (smsVibrateEnabled | reminderEnabled);
		int enable_disable = enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
				: PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		pm.setComponentEnabledSetting(cn, enable_disable,
				PackageManager.DONT_KILL_APP);
		MineLog.v("Set the MessageReceiver "
				+ (enable ? "Enabled" : "Disabled"));
	}

	public static void SetUpdateViewReceiverEnable(Context context,
			boolean enable) {
		PackageManager pm = context.getPackageManager();
		ComponentName cn = new ComponentName(context,
				MineUpdateViewReceiver.class);

		int enable_disable = enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
				: PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		pm.setComponentEnabledSetting(cn, enable_disable,
				PackageManager.DONT_KILL_APP);
		MineLog.v("Set the MineUpdateViewReceiver "
				+ (enable ? "Enabled" : "Disabled"));
	}

	public static void EnableMessageVibration(Context context, boolean enable) {
		initStatus(context);
		SetVibrationEnable(context, enable);

		SetMessageReceiver(context);
	}

	public static void EnableReminder(Context context, boolean enable) {
		initStatus(context);
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(context.getString(R.string.pref_reminder_enable_key),
				enable);

		// Commit the edits!
		editor.commit();

		if (!enable) {
			MineMessageReminderReceiver.cancelReminder(context,
					MineMessageReminderReceiver.REMINDER_TYPE_WHATEVER);
		}
		SetMessageReceiver(context);
	}

	public static void EnableReminderSound(Context context, boolean enable) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(context
				.getString(R.string.pref_reminder_sound_enable_key), enable);
		editor.commit();
	}

	public static boolean GetReminderEnabled(Context context) {
		ReminderEnabled = GetReminderEnabledPreference(context);
		return ReminderEnabled || GetAppAutoEnabled(context);
	}

	public static boolean GetReminderEnabledPreference(Context context) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getBoolean(context
				.getString(R.string.pref_reminder_enable_key), false);
	}

	public static boolean GetVibrationEnabled(Context context) {
		initStatus(context);
		return VibrateEnabled || GetAppAutoEnabled(context);
	}

	public static boolean GetVibrationEnabledPreference(Context context) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getBoolean(context
				.getString(R.string.pref_vibrate_enable_key), false);
	}

	public static boolean GetReminderSoundEnabled(Context context) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		ReminderSoundEnabled = settings.getBoolean(context
				.getString(R.string.pref_reminder_sound_enable_key), false);
		return ReminderSoundEnabled;
	}

	public static int GetReminderInterval(Context context) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		String minutes = settings.getString(context
				.getString(R.string.pref_reminder_interval_key), "5");
		int seconds = 60 * Integer.valueOf(minutes);
		return seconds;
	}

	public static String GetReminderSoundString(Context context) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		String reminder = settings.getString(context
				.getString(R.string.pref_reminder_sound_key),
				Settings.System.DEFAULT_NOTIFICATION_URI.toString());
		return reminder;
	}

	public static boolean IsFirstRun(Context context) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean ret = settings.getBoolean(context
				.getString(R.string.pref_first_time_run_key), true);
		if (ret) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(context
					.getString(R.string.pref_first_time_run_key), false);
			editor.commit();
		}
		return ret;
	}

	public static boolean IsUpgraded(Context context) {
		PackageManager manager = context.getPackageManager();
		int nowVersion = 0;
		try {
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			nowVersion = info.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		int storedVersion = GetVersion(context);
		if (nowVersion > storedVersion) {
			SetVersion(context, nowVersion);
			return true;
		}
		return false;
	}

	public static boolean IsWakeLockEnabled(Context context) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getBoolean(context
				.getString(R.string.pref_reminder_wakelock_enable_key), false);
	}

	public static boolean ShallNotify(Context context) {
		// If phone is in silent mode, don't notify
		int notifyMode = GetPhoneRingerState(context);
		if (notifyMode == AudioManager.RINGER_MODE_SILENT) {
			MineLog.v("phone is in silent mode");
			return false;
		} else {
			MineLog.v("phone is in mode: " + notifyMode);
		}
		
		// If user set bed time, and the time is in bedtime, don't notify
		boolean isInBedTime = false;
		if (GetReminderBedtimeEnabled(context)) {
			Calendar c = Calendar.getInstance();
			int curHour = c.get(Calendar.HOUR_OF_DAY);
			int curMin = c.get(Calendar.MINUTE);
			int[] bedTime = new int[4];
			GetReminderBedtimeTime(context, bedTime);
			int hourFrom = bedTime[0];
			int minFrom = bedTime[1];
			int hourTo = bedTime[2];
			int minTo = bedTime[3];

			// Calculate the bedtime interval from startTime
			// and compare the current interval from startTime
			// if the current interval is smaller, then we're in bedtime!
			int timeIntervalHour;
			if (hourFrom == hourTo && minFrom>minTo) {
				timeIntervalHour = 24;
			} else {
				timeIntervalHour = (hourFrom>hourTo)?
					(hourTo+24-hourFrom):(hourTo-hourFrom);
			}
			int timeIntervalMin = timeIntervalHour*60 + minTo-minFrom;
			int curIntervalHour = (hourFrom>curHour)?
					(curHour+24-hourFrom):(curHour-hourFrom);
			int curIntervalMin = curIntervalHour*60 +curMin-minFrom;
			if ( (curIntervalMin>=0) && (curIntervalMin < timeIntervalMin)) {
				isInBedTime = true;
			}
			MineLog.v("In bed time? " + isInBedTime);
			return !isInBedTime;
		}
		return true;
	}

	/**
	 * Parse the user provided custom vibrate pattern into a long[]
	 * 
	 */
	public static long[] parseVibratePattern(String stringPattern) {
		ArrayList<Long> arrayListPattern = new ArrayList<Long>();
		Long l;

		if (stringPattern == null)
			return null;

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

	public static void SetVibratePatternbyReason(Context context, int reason,
			String pattern) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		String key;
		if (reason == MineMessageVibrator.VIBRATE_REASON_REMINDER)
			key = context.getString(R.string.pref_reminder_vibrate_pattern_key);
		else
			key = context.getString(R.string.pref_sms_vibrate_pattern_key);
		editor.putString(key, pattern);

		// Commit the edits!
		editor.commit();
	}

	public static String GetVibratePatternbyReason(Context context, int reason) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		String key;
		if (reason == MineMessageVibrator.VIBRATE_REASON_REMINDER)
			key = context.getString(R.string.pref_reminder_vibrate_pattern_key);
		else
			key = context.getString(R.string.pref_sms_vibrate_pattern_key);
		return settings.getString(key, context
				.getString(R.string.pref_vibrate_pattern_default));
	}

	public static int GetVersion(Context context) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getInt(context
				.getString(R.string.pref_mine_message_vibrator_version_key), 0);
	}

	public static void SetVersion(Context context, int version) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(context
				.getString(R.string.pref_mine_message_vibrator_version_key),
				version);
		editor.commit();
	}

	private static int GetPhoneRingerState(Context context) {
		AudioManager am = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		return am.getRingerMode();
	}

	private static void SetVibrationEnable(Context context, boolean enable) {
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		// SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,
		// Context.MODE_PRIVATE);
		VibrateEnabled = enable;
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(context.getString(R.string.pref_vibrate_enable_key),
				enable);

		// Commit the edits!
		editor.commit();
	}

	/**
	 * When app is disabled and user switch to vibrate mode, enable app
	 * automatically;
	 * 
	 * @param context
	 */
	public static void EnableAppAuto(Context context) {
		boolean appEnabled = /* GetReminderEnabled(context) || */GetVibrationEnabled(context);
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		boolean appAutoEnabled = settings.getBoolean(context
				.getString(R.string.pref_app_auto_enable_key), true);

		if (!appEnabled && appAutoEnabled) {
			// enable app automatically
			editor
					.putBoolean(
							context
									.getString(R.string.pref_app_auto_vib_reminder_enabled_key),
							true);
			editor.commit();
			SetMessageReceiver(context);
			Intent intent = new Intent(
					MineVibrationToggler.VIBRATION_ACTION_NAME);
			context.sendBroadcast(intent);
		}
	}

	/**
	 * When app is automatically enabled, disable it here
	 * 
	 * @param context
	 */
	public static void DisableAppAuto(Context context) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();

		editor.putBoolean(context
				.getString(R.string.pref_app_auto_vib_reminder_enabled_key),
				false);
		editor.commit();
		SetMessageReceiver(context);
		Intent intent = new Intent(MineVibrationToggler.VIBRATION_ACTION_NAME);
		context.sendBroadcast(intent);
	}

	public static boolean GetAppAutoEnabled(Context context) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getBoolean(context
				.getString(R.string.pref_app_auto_vib_reminder_enabled_key),
				false);
	}

	public static void SetAppAutoEnablePreference(Context context,
			boolean enable) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(context.getString(R.string.pref_app_auto_enable_key),
				enable);
		editor.commit();
		if (!enable) {
			DisableAppAuto(context);
		}
	}

	public static boolean GetAppAutoEnablePreference(Context context) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getBoolean(context
				.getString(R.string.pref_app_auto_enable_key), false);
	}

	public static boolean GetMissedPhoneCallReminderEnabled(Context context) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getBoolean(context
					.getString(R.string.pref_reminder_item_missed_call_key), false);
	}
	
	public static boolean GetUnreadGmailReminderEnabled(Context context) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getBoolean(context
					.getString(R.string.pref_reminder_item_unread_gmail_key), false);
	}
	
	public static boolean GetReminderBedtimeEnabled(Context context) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getBoolean(context
				.getString(R.string.pref_reminder_bedtime_enable_key), false);
	}

	public static String GetReminderBedtimeTime(Context context) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getString(context.getString(
				R.string.pref_reminder_bedtime_time), "");
	}

	public static void GetReminderBedtimeTime(Context context, int[] bedTime) {
		String timePref = MineVibrationToggler.GetReminderBedtimeTime(context);
		String[] prefs = timePref.split(":");
		int hourFrom, minFrom, hourTo, minTo;
		if ( prefs.length == 4) {
			// we get the correct time preference
			try {
				hourFrom = Integer.parseInt(prefs[0]);
				minFrom = Integer.parseInt(prefs[1]);
				hourTo = Integer.parseInt(prefs[2]);
				minTo = Integer.parseInt(prefs[3]);				
			} catch (NumberFormatException ex) {
				MineLog.e("Error: unable to parse: " + timePref);
				hourFrom = 0;
				minFrom = 0;
				hourTo = 8;
				minTo = 0;
			}
		} else {
			// default time is 00:00 to 08:00
			MineLog.e("Error: timePref has invalid format: " + timePref);
			hourFrom = 0;
			minFrom = 0;
			hourTo = 8;
			minTo = 0;
		}
		bedTime[0]=hourFrom;
		bedTime[1] = minFrom;
		bedTime[2] = hourTo;
		bedTime[3] = minTo;
	}
	
	public static void SetReminderBedtimeTime(Context context, String time) {
		SharedPreferences settings = PreferenceManager
			.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(context.getString(
				R.string.pref_reminder_bedtime_time),
				time);
		editor.commit();
	}
}
