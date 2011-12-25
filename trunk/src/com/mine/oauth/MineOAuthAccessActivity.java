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
import com.mine.MineMessageUtils;
import com.mine.MineRingerModeChangeReceiver;
import com.mine.MineVibrationToggler;
import com.mine.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

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

		super.onNewIntent(intent);
	    Uri uri = intent.getData();

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
				// start to verify if the token account matches system account
				VerifyUserTokenTask task = new VerifyUserTokenTask(this);
				task.execute();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
	    }
	}
	
	private void authenticate() {
        GetRequestTokenTask task = new GetRequestTokenTask(this);
        task.execute();            
	}

	private class VerifyUserTokenTask extends AsyncTask<Void, Void, Boolean>
										implements OnDismissListener {
		private MineOAuthAccessActivity context;
		private ProgressDialog dialog;
		public VerifyUserTokenTask(MineOAuthAccessActivity act) {
			context = act;
        	dialog = new ProgressDialog(context);
        	dialog.setMessage("Verify Account...");
        	dialog.setIndeterminate(true);
        	dialog.setCancelable(true);
        	dialog.setOnDismissListener(this);
		}

		@Override
		protected void onPreExecute() {
            dialog.show();
        }

		@Override
		protected Boolean doInBackground(Void... arg0) {
			return MineMessageUtils.verifyGmailAccountWithToken(context, mAccessToken);
		}

		@Override
		protected void onPostExecute (Boolean result)  {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			if (result) {
				// verify OK
				MineVibrationToggler.SaveGmailToken(context, mAccessToken);
				MineVibrationToggler.SetUnreadGmailReminderEnabled(context,true);
				Intent intent1 = new Intent(MineRingerModeChangeReceiver.ACTION_GMAIL_TOKEN_CALLBACK);
				MineLog.v("Send gmail token callback intent");
				sendBroadcast(intent1);
				context.finish();
			} else {
				MineLog.e("Failed to verify the user account!");
				new AlertDialog.Builder(context).setMessage(
						String.format(getString(R.string.oauth_verify_dialog_text),
							MineMessageUtils.getGmailAccount(context)))
						.setPositiveButton(R.string.OK_string,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										/* User clicked OK so do some stuff */
									}
								}).create().show();
			}
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			if (this.cancel(true)) {
				MineLog.v("VerifyUserTokenTask cancelled onDismiss");				
			}
		}
	}

	private class GetRequestTokenTask extends AsyncTask<Void, Void, String>
										implements OnDismissListener {
		private Context context;
		private ProgressDialog dialog;
		public GetRequestTokenTask(MineOAuthAccessActivity act) {
			context = act;
        	dialog = new ProgressDialog(context);
        	dialog.setMessage("Loading...");
        	dialog.setIndeterminate(true);
        	dialog.setCancelable(true);
        	dialog.setOnDismissListener(this);
		}

		@Override
		protected void onPreExecute() {
            dialog.show();
        }

		@Override
		protected String doInBackground(Void... params) {
			String ret="";
			try {
				mHelper = new OAuthHelper("anonymous", "anonymous", "", "");
				ret = mHelper.getRequestToken();
				OAuthHelper.save(context,mHelper);
				startActivity(new Intent("android.intent.action.VIEW", Uri.parse(ret)).
        	    	setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
        	    			Intent.FLAG_ACTIVITY_NO_HISTORY |
        	    			Intent.FLAG_FROM_BACKGROUND));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return ret;
		}

		@Override
		protected void onPostExecute (String result)  {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			if (result.equals(""))
			Toast.makeText(context,
					R.string.oauth_dialog_fail,
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void onDismiss(DialogInterface arg0) {
			if (this.cancel(true)) {
				MineLog.v("Task cancelled onDismiss");				
			}
		}
	}
}

