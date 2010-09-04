package com.mine;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

public class MineVibrationToggler {

	public static void EnableMessageVibration(Context context, boolean enable) {
		PackageManager pm = context.getPackageManager();
		ComponentName cn = new ComponentName(context, MineMessageReceiver.class);
		int enable_disable = enable? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
				:PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		pm.setComponentEnabledSetting(cn, 
				enable_disable,
				PackageManager.DONT_KILL_APP);
	}
}
