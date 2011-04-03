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

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

public class MineReminderEnableCheckBoxPreference extends CheckBoxPreference {
	private Context context;

	public MineReminderEnableCheckBoxPreference(Context c, AttributeSet attrs,
			int defStyle) {
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

		MineVibrationSetting.AdjustPreference();
	}
}
