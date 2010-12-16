package com.mine;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class MineDonateDialog extends DialogPreference {

	private Context context;
	public static final Uri DONATE_URI = Uri
			.parse("http://mine-message-vibrator.appspot.com");

	public MineDonateDialog(Context c, AttributeSet attrs) {
		super(c, attrs);
		context = c;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			Intent i = new Intent(Intent.ACTION_VIEW, DONATE_URI);
			context.startActivity(i);
		}
	}
}
