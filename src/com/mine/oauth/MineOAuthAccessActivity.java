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

package com.mine.oauth;

import com.mine.MineLog;
import com.mine.MineRingerModeChangeReceiver;
import com.mine.MineVibrationToggler;
import com.mine.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MineOAuthAccessActivity extends Activity {
	private final static String LOGTAG = "MineOAuth";
	
	private static OAuthHelper mHelper;
	public static String[] mAccessToken = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(LOGTAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mineoauthnote);
		Button okButton = (Button) findViewById(R.id.oauth_note_ok);
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(LOGTAG, "go to authenticate...");
				authenticate();
			}
	    });
		Button koButton = (Button) findViewById(R.id.oauth_note_ko);
		koButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(LOGTAG, "go back...");
				MineOAuthAccessActivity.this.finish();
			}
	    });
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		Log.v(LOGTAG, "onNewIntent");
		boolean ret =false;
		super.onNewIntent(intent);
	    Uri uri = intent.getData();
	    // TODO: check if gmail username matches system's gmail user 
	    if (uri != null) {
		    //String token = uri.getQueryParameter("oauth_token");
		    String verifier = uri.getQueryParameter("oauth_verifier");
		    Log.v(LOGTAG, "get verifier: " + verifier);
			try {
				if (mHelper==null) {
					mHelper = OAuthHelper.load(getApplicationContext());
					if (mHelper == null) {
						Log.e(LOGTAG, "mHelper failed to load!");
						throw new Exception("mHelper failed to load!");
					}
				}
				mAccessToken = mHelper.getAccessToken(verifier);
				Log.v(LOGTAG, "get token: " + mAccessToken[0] +", " + mAccessToken[1]);
				MineVibrationToggler.SaveGmailToken(this, mAccessToken);
				MineVibrationToggler.SetUnreadGmailReminderEnabled(this,true);
				ret = true;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
	    }
		finish();
		
		if (ret) {
			Intent intent1 = new Intent(MineRingerModeChangeReceiver.ACTION_GMAIL_TOKEN_CALLBACK);
			MineLog.v("Send gmail token callback intent");
			sendBroadcast(intent1);
		}
	}
	
	private void authenticate() {
        try {
        	Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
	                try {
	    				mHelper = new OAuthHelper("anonymous", "anonymous", "", "");
	            	    //TODO: show a loading dialog
	            	    String uri = mHelper.getRequestToken();
	            	    OAuthHelper.save(getApplicationContext(),mHelper);
	
	            	    startActivity(new Intent("android.intent.action.VIEW", Uri.parse(uri)).
	            	    		setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_FROM_BACKGROUND));
	            	} catch (Exception ex) {
	            		ex.printStackTrace();
	            	}
                }
        	});
        	t.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

