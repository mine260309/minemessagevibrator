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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class MineRingerModeChangeReceiver extends BroadcastReceiver {

	private final String ACTION_RINGER_MODE_CHANGED = "android.media.RINGER_MODE_CHANGED";
	private final String ACTION_BOOT_COMPLETE = "android.intent.action.BOOT_COMPLETED";
	public static final String ACTION_GMAIL_TOKEN_CALLBACK = "com.mine.GMAIL_TOKEN_CALLBACK";
	private final String ACTION_GMAIL_CHANGED = "android.intent.action.PROVIDER_CHANGED";
	
	@Override
	public void onReceive(Context context, Intent i) {
		
		if (ACTION_RINGER_MODE_CHANGED.equals(i.getAction())) {
			int newMode = i.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1);
			MineLog.v("Receive New Ringer Mode" + newMode);
			handleNewRingerMode(context, newMode);
		}
		else if (ACTION_BOOT_COMPLETE.equals(i.getAction())) {
			MineLog.v("Receive BOOT_COMPLETE intent");
			if (MineVibrationToggler.GetMissedPhoneCallReminderEnabled(context))
			{
				MineLog.v("Start Telephony Listener automatically");
				MineTelephonyListenService.startTelephonyListener(context);
			}
			if (MineVibrationToggler.GetUnreadGmailReminderEnabled(context)) {
				MineLog.v("Start Gmail Watcher automatically");
				MineTelephonyListenService.startGmailWatcher(context);
			}
		}
		else if (ACTION_GMAIL_TOKEN_CALLBACK.equals(i.getAction())) {
			MineLog.v("On Gmail Token Callback");
			MineVibrationSetting.OnGmailTokenCallback(context);
		}
		else if (ACTION_GMAIL_CHANGED.equals(i.getAction())) {
			// This is kind of workaround:
			// On my CM9 for Milestone, the gmail watcher is not working,
			// so I have to register this receiver to get Gmail changed event.
			// Should I make this code only work on Android 4.0? I don't have 
			// any other devices to test...
			if (android.os.Build.VERSION.SDK_INT >= 11 /* VERSION CODE of HONEYCOMB */
				  && MineVibrationToggler.GetUnreadGmailReminderEnabled(context)) {
				Intent intent = new Intent(MineTelephonyListenService.ACTION_GMAIL_CHANGED);
				context.startService(intent);
			}
		}
	}

	private void handleNewRingerMode(Context context, int newMode) {
		/**
		 * If in silent mode, do nothing; If app is disabled and user switch to
		 * vibrate mode, automatically enable this app; And then if the user
		 * switch to ringer mode again, automatically revert the app
		 * */
		if (newMode == AudioManager.RINGER_MODE_SILENT)
			return;

		if (newMode == AudioManager.RINGER_MODE_VIBRATE) {
			boolean appEnabled = MineVibrationToggler
					.GetReminderEnabled(context)
					&& MineVibrationToggler.GetVibrationEnabled(context);
			if (!appEnabled) {
				MineVibrationToggler.EnableAppAuto(context);
			}
		} else {
			MineVibrationToggler.DisableAppAuto(context);
		}

	}

}
