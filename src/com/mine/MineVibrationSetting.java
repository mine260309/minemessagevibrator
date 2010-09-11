package com.mine;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MineVibrationSetting extends PreferenceActivity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    	//initStatus();
    }

    /** below function is not needed if we use defaultSharedPreference */
    /*
    private void initStatus() {
        boolean viabrateEnabled = MineVibrationToggler.GetVibrationMode(this);
        MineVibrationEnableCheckBoxPreference vibPref =
            (MineVibrationEnableCheckBoxPreference)findPreference(getString(R.string.pref_vibrate_enable_key));
        vibPref.setChecked(viabrateEnabled);
    }
    */
}