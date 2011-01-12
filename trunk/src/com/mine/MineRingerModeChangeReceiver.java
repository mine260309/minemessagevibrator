package com.mine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class MineRingerModeChangeReceiver extends BroadcastReceiver {

	private final String ACTION_RINGER_MODE_CHANGED = "android.media.RINGER_MODE_CHANGED";
	private final String ACTION_BOOT_COMPLETE = "android.intent.action.BOOT_COMPLETED";

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
