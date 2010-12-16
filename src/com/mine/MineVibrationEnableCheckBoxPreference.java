package com.mine;

import android.content.Context;
import android.content.Intent;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

public class MineVibrationEnableCheckBoxPreference extends CheckBoxPreference {
	private Context context;

	public MineVibrationEnableCheckBoxPreference(Context c, AttributeSet attrs,
			int defStyle) {
		super(c, attrs, defStyle);
		context = c;
	}

	public MineVibrationEnableCheckBoxPreference(Context c, AttributeSet attrs) {
		super(c, attrs);
		context = c;
	}

	public MineVibrationEnableCheckBoxPreference(Context c) {
		super(c);
		context = c;
	}

	@Override
	protected void onClick() {
		super.onClick();
		MineVibrationToggler.EnableMessageVibration(context, isChecked());

		Intent intent = new Intent(MineVibrationToggler.VIBRATION_ACTION_NAME);
		context.sendBroadcast(intent);

		MineVibrationSetting.AdjustPreference();
	}

}
