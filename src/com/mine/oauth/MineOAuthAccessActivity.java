/**
 * **********************************************************************
 * MineMessageVibrator is an Android App that provides vibrate and
 * reminder functions for SMS, MMS, Gmail, etc.
 * Copyright (C) 2010  Lei YU
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * **********************************************************************
 */

package com.mine.oauth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.mine.MineLog;
import com.mine.MineMessageUtils;
import com.mine.MineRingerModeChangeReceiver;
import com.mine.MineVibrationToggler;
import com.mine.R;

public class MineOAuthAccessActivity extends Activity {
  private final static String LOGTAG = "MineOAuth";
  public String[] mAccessToken = null;
  private OAuthHelper mHelper;
  private String TokenVerifier;

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
      TokenVerifier = uri.getQueryParameter("oauth_verifier");
      //Log.v(LOGTAG, "get verifier: " + TokenVerifier);
      try {
        if (mHelper == null) {
          mHelper = OAuthHelper.load(getApplicationContext());
          if (mHelper == null) {
            Log.e(LOGTAG, "mHelper failed to load!");
            throw new Exception("mHelper failed to load!");
          }
        }
        // start to get access token and verify
        // if its account matches system account
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

  private class VerifyUserTokenTask extends AsyncTask<Void, Void, Integer>
          implements OnDismissListener {
    private static final int VERIFY_OK = 0;
    private static final int VERIFY_MISMATCH = 1;
    private static final int VERIFY_NETWORK_ERROR = 2;

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
    protected Integer doInBackground(Void... arg0) {
      int ret = VERIFY_MISMATCH;
      try {
        mAccessToken = mHelper.getAccessToken(TokenVerifier);
        //Log.v(LOGTAG, "get token: " + mAccessToken[0] +", " + mAccessToken[1]);

        if (MineMessageUtils.verifyGmailAccountWithToken(context, mAccessToken)) {
          ret = VERIFY_OK;
        }
      } catch (Exception e) {
        ret = VERIFY_NETWORK_ERROR;
      }
      return ret;
    }

    @Override
    protected void onPostExecute(Integer result) {
      if (dialog.isShowing()) {
        dialog.dismiss();
      }
      if (result == VERIFY_OK) {
        // verify OK
        MineVibrationToggler.SaveGmailToken(context, mAccessToken);
        MineVibrationToggler.SetUnreadGmailReminderEnabled(context, true);
        Intent intent1 = new Intent(MineRingerModeChangeReceiver.ACTION_GMAIL_TOKEN_CALLBACK);
        MineLog.v("Send gmail token callback intent");
        sendBroadcast(intent1);
        context.finish();
      } else {
        MineLog.e("Failed to verify the user account!");
        String msg;
        if (result == VERIFY_NETWORK_ERROR) {
          msg = getString(R.string.oauth_dialog_fail);
        } else {
          msg = String.format(getString(R.string.oauth_verify_dialog_text),
                  MineMessageUtils.getGmailAccount(context));
        }
        new AlertDialog.Builder(context).setMessage(msg)
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
      String ret = "";
      try {
        mHelper = new OAuthHelper("anonymous", "anonymous", "", "");
        ret = mHelper.getRequestToken();
        OAuthHelper.save(context, mHelper);
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
    protected void onPostExecute(String result) {
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

