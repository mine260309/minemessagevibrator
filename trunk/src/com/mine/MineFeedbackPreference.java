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
		String texts = "Device Information: " + android.os.Build.MODEL + " " +
		   android.os.Build.DEVICE + " SDK " + 
		   android.os.Build.VERSION.SDK +
		   "\n*** If you don't want to share your device information," +
		   " you can just delete this text :)\n";
		emailIntent.putExtra(Intent.EXTRA_TEXT, texts);

		try {
		    context.startActivity(Intent.createChooser(emailIntent, "Feedback via email..."));
		} catch (android.content.ActivityNotFoundException ex) {
		    Toast.makeText(context, "There are no email clients installed.",
		    		Toast.LENGTH_SHORT).show();
		}
	}
}
