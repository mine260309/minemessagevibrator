package com.mine;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

public class MineReminderEnableCheckBoxPreference extends CheckBoxPreference {
	private Context context;
	
	public MineReminderEnableCheckBoxPreference(Context c, AttributeSet attrs, int defStyle) {
	    super(c, attrs, defStyle);
	    context = c;
	}
	
	public MineReminderEnableCheckBoxPreference(Context c, AttributeSet attrs) {
	    super(c, attrs);
	    context = c;
	}
	
	public MineReminderEnableCheckBoxPreference(Context c) {
	    super(c);
	    context = c;
	}
	
	@Override
	protected void onClick() {
	    super.onClick();
	    MineVibrationToggler.EnableReminder(context, isChecked());
	}
}
