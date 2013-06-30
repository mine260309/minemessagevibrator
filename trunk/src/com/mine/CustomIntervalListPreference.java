package com.mine;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.Toast;

public class CustomIntervalListPreference extends ListPreference {
	private Context context;
    private String customInterval;

	public CustomIntervalListPreference(Context c) {
		super(c);
		context = c;
		updateSummary();
	}

	public CustomIntervalListPreference(Context c, AttributeSet attrs) {
		super(c, attrs);
		context = c;
		updateSummary();
	}

	private void updateSummary() {
		int interval = MineVibrationToggler.GetReminderInterval(context);
		String summaryFormat = context.getString
				(R.string.pref_custom_interval_summary);

		String summary = String.format(summaryFormat, interval / 60);
		setSummary(summary);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		final SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		final String interval = settings.getString(getKey(),
				                  this.getEntries()[1].toString());

		if (positiveResult) {
			if ("Custom".equals(interval)) {
				showDialog();
			}
			else {
				updateSummary();			
			}
		}
	}

	private void showDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(context);

		alert.setTitle(context.getString(R.string.pref_custom_interval_title));
		alert.setMessage(context.getString(R.string.pref_custom_interval_helptext));

		// Set an EditText view to get user input 
		final EditText input = new EditText(context);
		// Show the current custom interval
		customInterval = String.valueOf(
		  MineVibrationToggler.getCustomReminderInterval(context));
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		input.setText(customInterval);
		input.setSelection(customInterval.length());
		alert.setView(input);

		alert.setPositiveButton(context.getString(android.R.string.ok),
				                new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    customInterval = input.getText().toString();
		    int interval = 0;
		    try {
		    	interval = Integer.parseInt(customInterval);
			    if (interval > 60*24) {
			    	// Interval too large
					Toast.makeText(
						context,
						context.getString(
								R.string.pref_custom_interval_toolarge),
								Toast.LENGTH_LONG).show();
			    }
			    else if (interval <= 0) {
			    	// Invalid interval
					Toast.makeText(
						context,
						context.getString(
								R.string.pref_custom_interval_bad),
								Toast.LENGTH_LONG).show();
			    }
			    else {
			      MineVibrationToggler.setCustomReminderInterval(context, interval);
			      updateSummary();
			    }
		    } catch(Exception ex) {
				Toast.makeText(
					context,
					context.getString(
							R.string.pref_custom_interval_bad),
							Toast.LENGTH_LONG).show();
		    }
		  }
		});
		alert.show();
	}
}
