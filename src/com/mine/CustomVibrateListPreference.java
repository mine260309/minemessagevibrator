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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.reflect.Field;

public class CustomVibrateListPreference extends ListPreference {

	private String vibrate_pattern_custom;
	private Context context;
	private int vibrate_reason;

	public CustomVibrateListPreference(Context c) {
		super(c);
		context = c;
		getVibratePreferenceReason();
	}

	public CustomVibrateListPreference(Context c, AttributeSet attrs) {
		super(c, attrs);
		context = c;
		getVibratePreferenceReason();
	}

	private void getVibratePreferenceReason() {
		String key = getKey();
		if (key.equals(context.getString(R.string.pref_vibration_mode_key))) {
			vibrate_reason = MineMessageVibrator.VIBRATE_REASON_SMS;
		} else if (key.equals(context.getString(R.string.pref_missed_call_notify_vib_key))) {
			vibrate_reason = MineMessageVibrator.VIBRATE_REASON_REMINDER_MISSEDCALL;
		} else if (key.equals(context.getString(R.string.pref_unread_gmail_notify_vib_key))) {
			vibrate_reason = MineMessageVibrator.VIBRATE_REASON_REMINDER_GMAIL;
		} else {
			vibrate_reason = MineMessageVibrator.VIBRATE_REASON_REMINDER;
		}
		//MineLog.v("Using Vibrate reason: " + vibrate_reason);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		final SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		final String pattern = settings.getString(getKey(),
				                 this.getEntries()[1].toString());

		MineLog.v("Get pattern of " + getKey() + " : " + pattern);
		if (positiveResult && "Custom".equals(pattern)) {
			showDialog();
		}
	}

	private void showDialog() {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View v = inflater.inflate(R.layout.vibratepatternlayout, null);

		final EditText et = (EditText) v
				.findViewById(R.id.CustomVibrateEditText);

		vibrate_pattern_custom = MineVibrationToggler
				.GetVibratePatternbyReason(context, vibrate_reason);
		et.setText(vibrate_pattern_custom);

		new AlertDialog.Builder(context).setIcon(
				android.R.drawable.ic_dialog_info).setTitle(
				R.string.pref_vibrate_pattern_title).setView(v)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String new_pattern = et.getText().toString();

								if (MineVibrationToggler.parseVibratePattern(et
										.getText().toString()) != null) {

									MineVibrationToggler
											.SetVibratePatternbyReason(context,
													vibrate_reason, new_pattern);

									Toast.makeText(
										context,
										context.getString(
												R.string.pref_vibrate_pattern_ok),
												Toast.LENGTH_SHORT).show();
								} else {

									/*
									 * No need to store anything if the contact
									 * pattern is invalid (just leave it as the
									 * last good value).
									 */

									Toast.makeText(
										context,
										context.getString(
												R.string.pref_vibrate_pattern_bad),
												Toast.LENGTH_SHORT).show();
								}
								try  
								{
									Field field = dialog.getClass()  
										.getSuperclass().getDeclaredField(  
										"mShowing");  
									field.setAccessible(true);
									// set mShowing to false to indicate dialog is closed
									field.set(dialog, true);  
									dialog.dismiss();  
								}  
								catch (Exception e)  
								{  
								} 
							}
						})
				.setNeutralButton(R.string.dialog_preview_vibrate_pattern, 
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
							int whichButton) {

							String new_pattern = et.getText().toString();
							long[] pattern = MineVibrationToggler.
								parseVibratePattern(new_pattern);
							if (pattern != null) {
								// add preview vibrate pattern code
								MineLog.v("Preview pattern...");
								MineMessageVibrator.vibrate(context, pattern);
							} else {
								Toast.makeText(
									context,
									context.getString(
										R.string.pref_vibrate_pattern_bad),
										Toast.LENGTH_SHORT).show();
							}
							try  
							{
								Field field = dialog.getClass()  
									.getSuperclass().getDeclaredField(  
									"mShowing");  
								field.setAccessible(true);
								// set mShowing to false to indicate dialog is closed
								field.set(dialog, false);  
								dialog.dismiss();  
							}  
							catch (Exception e)  
							{  
							} 
						}
					}
				)
				.show();
	}
}
