package com.mine;

import android.app.Dialog;
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
		String to = " to ";
		if (mHourFrom > mHourTo || 
			((mHourFrom==mHourTo)&& (mMinFrom > mMinTo)) ) {
			to = " to Tomorrow's ";
		}
		String appendix = "";
		int timeIntervalHour = (mHourFrom>=mHourTo)?
				(mHourTo+24-mHourFrom):(mHourTo-mHourFrom);
		if (timeIntervalHour >= 16) {// if user set more than 16 hour bed time
			appendix = " Are you sure?!";
		}
		String summary = new StringBuilder().append("Disable reminder from ")
        	.append(pad(mHourFrom)).append(":")
        	.append(pad(mMinFrom)).append(to)
        	.append(pad(mHourTo)).append(":")
        	.append(pad(mMinTo)).append(appendix).toString();
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
		super.getDialog().getCurrentFocus().clearFocus();
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
