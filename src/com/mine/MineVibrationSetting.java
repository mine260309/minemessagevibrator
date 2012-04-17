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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MineVibrationSetting extends PreferenceActivity
{
	public static final String ACTION_UPDATE_PREF_VIEW = "com.mine.UPDATE_PREF_VIEW";
	private static final int FIRST_TIME_RUN_DIALOG_ID = 1;
	private static final int UPGRADED_RUN_DIALOG_ID = 2;
	private static final int GMAIL_TOKEN_INVALID_DIALOG_ID = 3;
	private static Context context;
	private static MineVibrationSetting prefContext;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = MineVibrationTabView.getContext();
		//context = this;
		prefContext = this;
		if (MineVibrationToggler.IsUpgraded(this)) {
			// There is an upgrade, show upgrade dialog
			showDialog(UPGRADED_RUN_DIALOG_ID);
		}
		if (MineVibrationToggler.IsFirstRun(this)) {
			// This is the first time running, show a help screen
			showDialog(FIRST_TIME_RUN_DIALOG_ID);
		}
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Send intent to update the preference view
		MineVibrationToggler.SetUpdateViewReceiverEnable(context, true);
		Intent intent = new Intent(ACTION_UPDATE_PREF_VIEW);
		prefContext.sendBroadcast(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// show dialog according to the id
		int msgStringId;
		int titleStringId;
		if (id == FIRST_TIME_RUN_DIALOG_ID) {
			msgStringId = R.string.first_run_dialog_message;
			titleStringId = R.string.first_run_dialog_title;
		} else if (id == UPGRADED_RUN_DIALOG_ID){
			msgStringId = R.string.upgraded_run_dialog_message;
			titleStringId = R.string.upgraded_run_dialog_title;
		} else if (id == GMAIL_TOKEN_INVALID_DIALOG_ID) {
			msgStringId = R.string.token_invalidate_dialog_text;
			titleStringId = R.string.token_invalidate_dialog_title;
		} else {
			MineLog.e("Unkonw dialog id!");
			return new Dialog(this);
		}
		return new AlertDialog.Builder(this).setMessage(
				getString(msgStringId)).setTitle(
				getString(titleStringId))
				.setPositiveButton(R.string.OK_string,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								/* User clicked OK so do some stuff */
							}
						}).create();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.addSubMenu(0, FIRST_TIME_RUN_DIALOG_ID, 0,
				R.string.menu_help_string);
		menu.addSubMenu(0, UPGRADED_RUN_DIALOG_ID, 0,
				R.string.menu_whatsnew_string);

		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case FIRST_TIME_RUN_DIALOG_ID:
			showDialog(FIRST_TIME_RUN_DIALOG_ID);
			break;
		case UPGRADED_RUN_DIALOG_ID:
			showDialog(UPGRADED_RUN_DIALOG_ID);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
		finish();
	}

	public static void AdjustPreference() {

		// re-adjust the contents based on the logic
		Preference vibPref = prefContext.findPreference(context
				.getString(R.string.pref_vibrate_enable_key));
		Preference remPref = prefContext.findPreference(context
				.getString(R.string.pref_reminder_enable_key));
		Preference remSoundPref = prefContext.findPreference(context
				.getString(R.string.pref_reminder_sound_enable_key));

		if (vibPref == null || remPref == null || remSoundPref == null) {
			MineLog.e("Can't find pref");
			return;
		}
		boolean vib = MineVibrationToggler
				.GetVibrationEnabledPreference(context);
		boolean auto = MineVibrationToggler.GetAppAutoEnablePreference(context);
		boolean rem = MineVibrationToggler
				.GetReminderEnabledPreference(context);
		boolean remSound = MineVibrationToggler
				.GetReminderSoundEnabled(context)
				&& rem;
//		MineLog.v("vib:" + vib + " auto:" + auto + " rem:" + rem + " remSound:"
//				+ remSound);

		if (!vib && !auto) {
			vibPref.notifyDependencyChange(true);
		} else if (!vib && auto) {
			vibPref.notifyDependencyChange(false);
		} else if (vib && !auto) {
			vibPref.notifyDependencyChange(false);
		} else {
			vibPref.notifyDependencyChange(false);
		}

		if (!rem && !auto) {
			remPref.notifyDependencyChange(true);
		} else if (!rem && auto) {
			remPref.notifyDependencyChange(false);
		} else if (rem && !auto) {
			remPref.notifyDependencyChange(false);
		} else {
			remPref.notifyDependencyChange(false);
		}

		if (!remSound && !auto) {
			remSoundPref.notifyDependencyChange(true);
		} else if (!remSound && auto) {
			remSoundPref.notifyDependencyChange(false);
		} else if (remSound && !auto) {
			remSoundPref.notifyDependencyChange(false);
		} else {
			remSoundPref.notifyDependencyChange(false);
		}
	}

	// This function shall be called once, and only once when app starts
	public static void InitAdjustPreference() {
		if (context == null) {
			MineLog.e("InitAdjustPreference: context null!");
			return;
		}
		MineVibrationToggler.SetUpdateViewReceiverEnable(context, false);
		AdjustPreference();
		PostInitServices();
	}

	/** this function is called to initialize necessary services if not started */
	private static void PostInitServices() {
		// check the Telephony Listener
		if (MineVibrationToggler.GetUnreadGmailReminderEnabled(context)) {
			MineTelephonyListenService.startGmailWatcher(prefContext);
		}
		// check the gmail watcher
		if (MineVibrationToggler.GetMissedPhoneCallReminderEnabled(context)) {
			MineTelephonyListenService.startTelephonyListener(prefContext);
		}
		if (MineVibrationToggler.getGmailTokenInvalid(context)) {
			prefContext.showDialog(GMAIL_TOKEN_INVALID_DIALOG_ID);
		}
	}
	
	public static void OnGmailTokenCallback(Context context) {
		String[] token = MineVibrationToggler.GetGmailToken(context);
		if (token[0].equals("") || token[1].equals("")) {
			MineLog.v("OnGmailTokenCallback, got empty token");
			return;
		}
		else {
			if (prefContext == null){
				MineLog.v("appContext is null, let it restart");
				// app is killed, let it restart
				return;
			}
			else {
				EnableGmailReminderCheckbox(prefContext);
				MineTelephonyListenService.startGmailWatcher(context);
			}
		}
	}

	private static void EnableGmailReminderCheckbox(MineVibrationSetting context) {
		CheckBoxPreference gmailPref = (CheckBoxPreference) context.findPreference(context
				.getString(R.string.pref_reminder_item_unread_gmail_key));
		if (gmailPref == null) {
			MineLog.e("Can't find gmail pref");
			return;
		}
		gmailPref.setChecked(true);
	}
}