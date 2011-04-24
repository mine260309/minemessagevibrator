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
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

public class MineTimePickerPreference extends DialogPreference
    implements TimePicker.OnTimeChangedListener {

	private Context context;
	private int mHourFrom, mMinFrom;
	private int mHourTo, mMinTo;
	
	private int tempHourFrom, tempMinFrom;
	private int tempHourTo, tempMinTo;
	
	private TimePicker TimePickerFrom;
	private TimePicker TimePickerTo;
	
	public MineTimePickerPreference(Context c, AttributeSet attrs) {
		super(c, attrs);
		context = c;
		init();
	}

	public MineTimePickerPreference(Context c, AttributeSet attrs,
			int defStyle) {
		super(c, attrs, defStyle);
		context = c;
		init();
	}
	
	private static String pad(int c) {
	    if (c >= 10)
	        return String.valueOf(c);
	    else
	        return "0" + String.valueOf(c);
	}
	
	private void updateSummary() {
		// Summary:
		// Disable reminder from %02d:%02d to  
		// [Tomorrow's ]%02d:%02d[ Are you sure?!]
		
		String summaryFormat = context.getString
			(R.string.pref_reminder_bedtime_time_summary);
		String tomorrow = "";
		String appendix = "";
		String summary;
		
		if (mHourFrom > mHourTo || 
			((mHourFrom==mHourTo)&& (mMinFrom > mMinTo)) ) {
			tomorrow = context.getString(
				R.string.pref_reminder_bedtime_time_summary_tomorrow);
		}
		int timeIntervalHour = (mHourFrom>=mHourTo)?
				(mHourTo+24-mHourFrom):(mHourTo-mHourFrom);
		if (timeIntervalHour >= 16) {// if user set more than 16 hour bed time
			appendix = context.getString(
				R.string.pref_reminder_bedtime_time_summary_areyousure);
		}
		summary = String.format(summaryFormat, pad(mHourFrom),
				pad(mMinFrom), tomorrow, pad(mHourTo), pad(mMinTo),
				appendix);
		setSummary(summary);
	}
	private void init() {
		setPersistent(true);

		int[] bedTime = new int[4];
		MineVibrationToggler.GetReminderBedtimeTime(context, bedTime);
		mHourFrom = bedTime[0];
		mMinFrom = bedTime[1];
		mHourTo = bedTime[2];
		mMinTo = bedTime[3];

		tempHourFrom = mHourFrom;
		tempMinFrom = mMinFrom;
		tempHourTo = mHourTo;
		tempMinTo = mMinTo;
		updateSummary();
	}

	@Override
	protected void onBindDialogView (View view) {
		super.onBindDialogView(view);
		TimePickerFrom = (TimePicker)view.findViewById(R.id.TimePickerBedTimeFrom);
		TimePickerTo = (TimePicker)view.findViewById(R.id.TimePickerBedTimeTo);
		boolean isNeed24hour = DateFormat.is24HourFormat(context);
		TimePickerFrom.setIs24HourView(isNeed24hour);
		TimePickerTo.setIs24HourView(isNeed24hour);
		TimePickerFrom.setCurrentHour(mHourFrom);
		TimePickerTo.setCurrentHour(mHourTo);
		TimePickerFrom.setCurrentMinute(mMinFrom);
		TimePickerTo.setCurrentMinute(mMinTo);

		TimePickerFrom.setOnTimeChangedListener(this);
		TimePickerTo.setOnTimeChangedListener(this);
	}
	
	@Override 
	protected void onDialogClosed (boolean positiveResult) {
		if (positiveResult) {
			// Save preferences
			mHourFrom = tempHourFrom;
			mMinFrom = tempMinFrom;
			mHourTo = tempHourTo;
			mMinTo = tempMinTo;
			
			String time = mHourFrom+":"+mMinFrom+":"+mHourTo+":"+mMinTo;
			MineLog.v("Set bed time to "+time);
			MineVibrationToggler.SetReminderBedtimeTime(context, time);
			updateSummary();
		}
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if ( super.getDialog() != null && 
			 super.getDialog().getCurrentFocus()!=null ) {
			super.getDialog().getCurrentFocus().clearFocus();
		}
		super.onClick(dialog, which);
	}

	@Override
	public void onTimeChanged(TimePicker view, int hour, int min) {
		if (view.equals(TimePickerFrom)) {
			tempHourFrom = hour;
			tempMinFrom = min;
		} else if (view.equals(TimePickerTo)) {
			tempHourTo = hour;
			tempMinTo = min;
		} else {
			MineLog.e("unknown view");
		}
	}
}
