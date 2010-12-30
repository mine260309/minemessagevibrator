package com.mine;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MineVibrationSetting extends PreferenceActivity
	implements ServiceConnection
{
	public static final String ACTION_UPDATE_PREF_VIEW = "com.mine.UPDATE_PREF_VIEW";

	private static final int FIRST_TIME_RUN_DIALOG_ID = 1;
	private static final int UPGRADED_RUN_DIALOG_ID = 2;
	private static MineVibrationSetting context;

	// flag indicate InitAdjustPreference is called
	// private static boolean InitAdjustPreferenceCalled = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		if (MineVibrationToggler.IsUpgraded(this)) {
			// There is an upgrade, show upgrade dialog
			showDialog(UPGRADED_RUN_DIALOG_ID);
		}
		if (MineVibrationToggler.IsFirstRun(this)) {
			// This is the first time running, show a help screen
			showDialog(FIRST_TIME_RUN_DIALOG_ID);
		}
		addPreferencesFromResource(R.xml.preferences);
		
		Intent intent = new Intent(MineTelephonyListenService.ACTION_START_TELEPHONY_LISTEN);
		intent.setClass(context, MineTelephonyListenService.class);
		bindService(intent, (ServiceConnection) context, 0);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Send intent to update the preference view
		MineVibrationToggler.SetUpdateViewReceiverEnable(context, true);
		Intent intent = new Intent(ACTION_UPDATE_PREF_VIEW);
		MineLog.v("Send update pref view intent");
		context.sendBroadcast(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(this);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// show dialog according to the id
		if (id == FIRST_TIME_RUN_DIALOG_ID) {
			return new AlertDialog.Builder(this).setMessage(
					getString(R.string.first_run_dialog_message)).setTitle(
					getString(R.string.first_run_dialog_title))
					.setPositiveButton(R.string.OK_string,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* User clicked OK so do some stuff */
								}
							}).create();
		} else {
			return new AlertDialog.Builder(this).setMessage(
					getString(R.string.upgraded_run_dialog_message)).setTitle(
					getString(R.string.upgraded_run_dialog_title))
					.setPositiveButton(R.string.OK_string,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* User clicked OK so do some stuff */
								}
							}).create();
		}
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

	public static void AdjustPreference() {

		// re-adjust the contents based on the logic
		Preference vibPref = context.findPreference(context
				.getString(R.string.pref_vibrate_enable_key));
		Preference remPref = context.findPreference(context
				.getString(R.string.pref_reminder_enable_key));
		Preference remSoundPref = context.findPreference(context
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
		MineLog.v("vib:" + vib + " auto:" + auto + " rem:" + rem + " remSound:"
				+ remSound);

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
		// InitAdjustPreferenceCalled = true;
		MineVibrationToggler.SetUpdateViewReceiverEnable(context, false);
		AdjustPreference();
	}


	public void onServiceConnected(ComponentName name, IBinder service) {
		MineLog.v("Conntect to service "+name);
	}


	public void onServiceDisconnected(ComponentName name) {
		MineLog.v("Disconntect to service "+name);
	}
}