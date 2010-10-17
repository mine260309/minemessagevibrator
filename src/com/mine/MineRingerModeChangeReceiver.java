package com.mine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class MineRingerModeChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent i) {
		// TODO Auto-generated method stub
		int newMode = i.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1);
		MineLog.v("Receive New Ringer Mode" + newMode);
		handleNewRingerMode(context, newMode);
	}
	
	private void handleNewRingerMode(Context context, int newMode) {
		/**
		 * If in silent mode, do nothing;
		 * If app is disabled and user switch to vibrate mode, automatically enable this app;
		 * And then if the user switch to ringer mode again, automatically revert the app
		 * */
		if (newMode == AudioManager.RINGER_MODE_SILENT)
			return;
		
		if (newMode == AudioManager.RINGER_MODE_VIBRATE) {
			boolean appEnabled = MineVibrationToggler.GetReminderEnabled(context)
				&& MineVibrationToggler.GetVibrationEnabled(context);
			if (!appEnabled) {
				MineVibrationToggler.EnableAppAuto(context);
			}
		} else {
			MineVibrationToggler.DisableAppAuto(context);
		}
			
	}

}
