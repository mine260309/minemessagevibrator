package com.mine;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;
import android.widget.Toast;

public class MineFeedbackPreference extends Preference {

	private final String[] EMAIL_ADDR = {"mine260309@gmail.com"};
	private final String EMAIL_SUBJECT = "Feedback of MineMessageVibrator";
	
	private Context context;

	public MineFeedbackPreference(Context c) {
		super(c);
		context = c;
	}

	public MineFeedbackPreference(Context c, AttributeSet attrs) {
		super(c, attrs);
		context = c;
	}

	public MineFeedbackPreference(Context c, AttributeSet attrs,
			int defStyle) {
		super(c, attrs, defStyle);
		context = c;
	}

	@Override
	protected void onClick() {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("message/rfc822");
		emailIntent.putExtra(Intent.EXTRA_EMAIL, EMAIL_ADDR);
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, EMAIL_SUBJECT);
		try {
		    context.startActivity(Intent.createChooser(emailIntent, "Feedback via email..."));
		} catch (android.content.ActivityNotFoundException ex) {
		    Toast.makeText(context, "There are no email clients installed.",
		    		Toast.LENGTH_SHORT).show();
		}
	}
}
