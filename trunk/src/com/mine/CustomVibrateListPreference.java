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
		if (getKey()
				.equals(context.getString(R.string.pref_vibration_mode_key))) {
			vibrate_reason = MineMessageVibrator.VIBRATE_REASON_SMS;
		} else {
			vibrate_reason = MineMessageVibrator.VIBRATE_REASON_REMINDER;
		}
		MineLog.v("Using Vibrate reason: " + vibrate_reason);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		String pattern;

		pattern = settings.getString(getKey(), this.getEntries()[1].toString());

		MineLog.v("Get pattern of " + getKey() + ": " + pattern);
		if (positiveResult) {

			if ("Custom".equals(pattern)) {
				showDialog();
			}
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

									Toast
											.makeText(
													context,
													context
															.getString(R.string.pref_vibrate_pattern_ok),
													Toast.LENGTH_SHORT).show();

								} else {

									/*
									 * No need to store anything if the contact
									 * pattern is invalid (just leave it as the
									 * last good value).
									 */

									Toast
											.makeText(
													context,
													context
															.getString(R.string.pref_vibrate_pattern_bad),
													Toast.LENGTH_SHORT).show();
								}
							}
						}).show();
	}
}