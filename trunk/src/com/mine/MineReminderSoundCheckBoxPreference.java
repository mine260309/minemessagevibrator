package com.mine;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

public class MineReminderSoundCheckBoxPreference extends CheckBoxPreference {
	private Context context;

	public MineReminderSoundCheckBoxPreference(Context c) {
		super(c);
		context = c;
	}

	public MineReminderSoundCheckBoxPreference(Context c,
			AttributeSet attrs) {
		super(c, attrs);
		context = c;
	}

	public MineReminderSoundCheckBoxPreference(Context c,
			AttributeSet attrs, int defStyle) {
		super(c, attrs, defStyle);
		context = c;
	}
	
	@Override
	protected void onClick() {
	    super.onClick();
	    MineVibrationToggler.EnableReminderSound(context, isChecked());
	    
		MineVibrationSetting.AdjustPreference();
	}
}
