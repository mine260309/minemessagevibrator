package com.mine;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

public class MineReminderMissedPhoneCallCheckbox extends CheckBoxPreference {
	private Context context;

	public MineReminderMissedPhoneCallCheckbox(Context c) {
		super(c);
		context = c;
	}

	public MineReminderMissedPhoneCallCheckbox(Context c,
			AttributeSet attrs) {
		super(c, attrs);
		context = c;
	}

	public MineReminderMissedPhoneCallCheckbox(Context c,
			AttributeSet attrs, int defStyle) {
		super(c, attrs, defStyle);
		context = c;
	}

	@Override
	protected void onClick() {
		super.onClick();
		//MineVibrationToggler.SetMissedPhoneCallReminderEnabled(context, isChecked());
		//MineVibrationSetting.AdjustPreference();
		if(isChecked()) {
			Intent intent = new Intent(MineTelephonyListenService.ACTION_START_TELEPHONY_LISTEN);
			intent.setClass(context, MineTelephonyListenService.class);
			context.startService(intent);
			context.bindService(intent, (ServiceConnection) context, 0);
		}
	}

}
