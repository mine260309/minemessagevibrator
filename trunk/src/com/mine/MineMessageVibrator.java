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

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

public class MineMessageVibrator {
	// private static final int MINE_MESSAGE_NOTIFICATION_ID = 0x1086;
	// private static Notification smsNotify = null;
	// private static Notification mmsNotify = null;
	public static final int VIBRATE_REASON_SMS = 1;
	public static final int VIBRATE_REASON_MMS = VIBRATE_REASON_SMS;
	public static final int VIBRATE_REASON_REMINDER = 0x80;
	public static final int VIBRATE_REASON_GMAIL = 2;
	public static final int VIBRATE_REASON_MISSEDCALL = 3;
	public static final int VIBRATE_REASON_REMINDER_MESSAGE = 0x81;
	public static final int VIBRATE_REASON_REMINDER_GMAIL = 0x82;
	public static final int VIBRATE_REASON_REMINDER_MISSEDCALL = 0x83;
	
	private static final String VIBRATE_MODE_SHORT = "Short";
	private static final String VIBRATE_MODE_MIDDLE = "Middle";
	private static final String VIBRATE_MODE_LONG = "Long";
	private static final String VIBRATE_MODE_MULTIPLE_SHORT = "Multiple Short";
	private static final String VIBRATE_MODE_MULTIPLE_MIDDLE = "Multiple Middle";
	private static final String VIBRATE_MODE_SAME_AS_MESSAGE = "Same as Message";
	private static final String VIBRATE_MODE_CUSTOM = "Custom";

	private static final long[] VibratePatternShort = new long[] { 0, 500 };
	private static final long[] VibratePatternMiddle = new long[] { 0, 1200 };
	private static final long[] VibratePatternLong = new long[] { 0, 2000 };
	private static final long[] VibratePatternMultipleShort = new long[] { 0,
			500, 200, 500, 200, 500 };
	private static final long[] VibratePatternMultipleMiddle = new long[] { 0,
			1200, 300, 1200, 300, 1200 };

	public static void notifySMS(Context context) {
		MineLog.v("notifying SMS");
		vibrate(context, VIBRATE_REASON_SMS);
	}

	public static void notifyMMS(Context context) {
		MineLog.v("notifying MMS");
		vibrate(context, VIBRATE_REASON_MMS);
	}

	public static void notifyGmail(Context context) {
		MineLog.v("notifying Gmail");
		vibrate(context, VIBRATE_REASON_GMAIL);
	}
	
	public static void notifyReminder(Context context, int type) {
		MineLog.v("notifying reminder, type: " + type);
		int reason;
		switch(type) {
		case MineMessageReminderReceiver.REMINDER_TYPE_MESSAGE:
			reason = VIBRATE_REASON_REMINDER_MESSAGE;
			break;
		case MineMessageReminderReceiver.REMINDER_TYPE_GMAIL:
			reason = VIBRATE_REASON_REMINDER_GMAIL;
			break;
		case MineMessageReminderReceiver.REMINDER_TYPE_PHONECALL:
			reason = VIBRATE_REASON_REMINDER_MISSEDCALL;
			break;
		default:
			reason = VIBRATE_REASON_REMINDER;
			break;
		}
		vibrate(context, reason);
	}
	
	// This function is used in preview of custom vibrate pattern
	public static void vibrate(Context context, long[] pattern) {
		Vibrator v = (Vibrator) context
			.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(pattern, -1);
	}

	private static void vibrate(Context context, int reason) {
		// If the phone is in silent mode, do not vibrate
		if (MineVibrationToggler.ShallNotify(context)) {
			// Get phone state, if not idle then don't vibrate
			TelephonyManager mTM = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (mTM.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
				// Check whether we need to ring or vibrate, or both...
				boolean needSound = ((reason & VIBRATE_REASON_REMINDER) !=0)
						&& MineVibrationToggler
								.GetReminderSoundEnabled(context);
				boolean needVibrate = MineVibrationToggler
						.GetVibrationEnabled(context);
				// MineLog.v("VibrationEnabled is " +
				// MineVibrationToggler.GetVibrationEnabled(context)
				// + "\n ReminderVibrateEnabled is " +
				// MineVibrationToggler.GetReminderVibrateEnabled(context));
				if (needSound) {
					NotificationManager nm = (NotificationManager) context
							.getSystemService(Context.NOTIFICATION_SERVICE);
					Notification n = new Notification();

					// Try and parse the user preference, use the default if it
					// fails
					Uri reminderSoundURI = Uri.parse(MineVibrationToggler
							.GetReminderSoundString(context, reason));

					n.sound = reminderSoundURI;
					nm.notify(reason, n);
					MineLog.v("Playing sound " + reminderSoundURI);
				}

				if (needVibrate) {
					Vibrator v = (Vibrator) context
							.getSystemService(Context.VIBRATOR_SERVICE);
					v.vibrate(GetVibratePattern(context, reason), -1);
					MineLog.v("Vibrating... ");
				}
			} else {
				MineLog.v("phone in call, not vibrate");
			}
		} else {
			MineLog.v("phone in silent mode or in bed time, not vibrate");
		}
	}

	private static long[] GetVibratePattern(Context context, int reason) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		String pattern;
		long[] pat = null;
		if ( (reason & VIBRATE_REASON_REMINDER) != 0 ) {
			switch(reason&0x0F) {
			case VIBRATE_REASON_GMAIL:
				pattern = settings.getString(context
						.getString(R.string.pref_unread_gmail_notify_vib_key),
						VIBRATE_MODE_SAME_AS_MESSAGE);
				break;
			case VIBRATE_REASON_MISSEDCALL:
				pattern = settings.getString(context
						.getString(R.string.pref_missed_call_notify_vib_key),
						VIBRATE_MODE_SAME_AS_MESSAGE);
				break;
			default:
				pattern = settings.getString(context
						.getString(R.string.pref_reminder_vibration_mode_key),
						VIBRATE_MODE_SAME_AS_MESSAGE);
			}

			if (pattern.equals(VIBRATE_MODE_SAME_AS_MESSAGE)) {
				reason = VIBRATE_REASON_SMS;
			} else if (pattern.equals(VIBRATE_MODE_SHORT)) {
				pat = VibratePatternShort;
				MineLog.v("Reminder Vibrate using pattern " + pattern);
			} else if (pattern.equals(VIBRATE_MODE_MIDDLE)) {
				pat = VibratePatternMiddle;
				MineLog.v("Reminder Vibrate using pattern " + pattern);
			} else if (pattern.equals(VIBRATE_MODE_LONG)) {
				pat = VibratePatternLong;
				MineLog.v("Reminder Vibrate using pattern " + pattern);
			} else if (pattern.equals(VIBRATE_MODE_MULTIPLE_SHORT)) {
				pat = VibratePatternMultipleShort;
				MineLog.v("Reminder Vibrate using pattern " + pattern);
			} else if (pattern.equals(VIBRATE_MODE_MULTIPLE_MIDDLE)) {
				pat = VibratePatternMultipleMiddle;
				MineLog.v("Reminder Vibrate using pattern " + pattern);
			} else if (pattern.equals(VIBRATE_MODE_CUSTOM)) {
				pat = MineVibrationToggler.parseVibratePattern(
						MineVibrationToggler.
						GetVibratePatternbyReason(context, reason));
				if (pat == null) {
					MineLog.e("Parse Custom Pattern Error, using defalt Middle");
					pat = VibratePatternMiddle;
				}
				MineLog.v("Vibrate using custom pattern " + pat.toString());
			} else {
				// default, use same as message
				reason = VIBRATE_REASON_SMS;
			}
		}
		if ((reason & VIBRATE_REASON_REMINDER) == 0) {
			// it's not a reminder
			pattern = settings.getString(context
					.getString(R.string.pref_vibration_mode_key), "Middle");
			if (pattern.equals(VIBRATE_MODE_SHORT)) {
				pat = VibratePatternShort;
				MineLog.v("Vibrate using pattern " + pattern);
			} else if (pattern.equals(VIBRATE_MODE_MIDDLE)) {
				pat = VibratePatternMiddle;
				MineLog.v("Vibrate using pattern " + pattern);
			} else if (pattern.equals(VIBRATE_MODE_LONG)) {
				pat = VibratePatternLong;
				MineLog.v("Vibrate using pattern " + pattern);
			} else if (pattern.equals(VIBRATE_MODE_MULTIPLE_SHORT)) {
				pat = VibratePatternMultipleShort;
				MineLog.v("Vibrate using pattern " + pattern);
			} else if (pattern.equals(VIBRATE_MODE_MULTIPLE_MIDDLE)) {
				pat = VibratePatternMultipleMiddle;
				MineLog.v("Vibrate using pattern " + pattern);
			} else if (pattern.equals(VIBRATE_MODE_CUSTOM)) {
				pat = MineVibrationToggler
						.parseVibratePattern(settings
								.getString(
										context
												.getString(R.string.pref_sms_vibrate_pattern_key),
										context
												.getString(R.string.pref_vibrate_pattern_default)));
				if (pat == null) {
					MineLog
							.e("Parse Custom Pattern Error, using defalt Middle");
					pat = VibratePatternMiddle;
				}
				MineLog.v("Vibrate using custom pattern " + pat.toString());
			} else {
				// default, use middle
				pat = VibratePatternMiddle;
				MineLog.v("Vibrate using pattern " + "default");
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
