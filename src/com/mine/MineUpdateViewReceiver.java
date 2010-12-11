package com.mine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MineUpdateViewReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if( intent.getAction().equals(MineVibrationSetting.ACTION_UPDATE_PREF_VIEW) ) {
			// this piece of code is put here just because it's simple...
			MineLog.v("Update preference view");
			MineVibrationSetting.InitAdjustPreference();
			return;
		}
	}

}
