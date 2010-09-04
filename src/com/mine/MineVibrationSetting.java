package com.mine;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.*;
import android.os.Bundle;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.view.*;
import android.media.AudioManager;

public class MineVibrationSetting extends Activity {
	
	private ListView mainSettingListView;
	private ArrayAdapter<String> mainListAdapter;
	private AudioManager mineAudioManager;
	private Context mineContext;
	private static final int VibrationModes[] = {AudioManager.VIBRATE_SETTING_OFF,
			AudioManager.VIBRATE_SETTING_ON,
			AudioManager.VIBRATE_SETTING_ONLY_SILENT};
	private static final int RingerModes[] = {AudioManager.RINGER_MODE_NORMAL,
		AudioManager.RINGER_MODE_SILENT,
		AudioManager.RINGER_MODE_VIBRATE
	};
	
	private boolean messageCheckerEnabled = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mineContext = this;
        mineAudioManager = (AudioManager) mineContext.getSystemService(Context.AUDIO_SERVICE);
        mainSettingListView = new ListView(this);
        
        mainListAdapter = new ArrayAdapter<String>(this, R.layout.textview);
        mainSettingListView.setAdapter(mainListAdapter);
    	mainListAdapter.add("Toogle Setting");
//    	mainListAdapter.add("Current mode: " 
//    			+ getVibrationString(mineAudioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION)));
//    			+ getRingerString(mineAudioManager.getRingerMode()));
    	
    	mainSettingListView.setOnItemClickListener(new OnItemClickListener() {
			    public void onItemClick(AdapterView<?> parent, View view,
			        int position, long id) {
			      // When clicked, do something
			    	String menuItem = ((TextView) view).getText().toString();
			    	if (menuItem == "Toogle Setting") {
			    		//ToogleVibrationMode();
			    		//ToogleRingerMode();
			    		ToogleMessageChecker();
			    	}
			    }
			  });
		setContentView(mainSettingListView);
		
        //setContentView(R.layout.main);
    }
    
    private String getVibrationString(int type) {
    	String virbTypeStr;
		switch(type) {
		case AudioManager.VIBRATE_SETTING_OFF:
			virbTypeStr = "Vibration OFF";
			break;
		case AudioManager.VIBRATE_SETTING_ON:
			virbTypeStr = "Vibration ON";
			break;
		case AudioManager.VIBRATE_SETTING_ONLY_SILENT:
			virbTypeStr = "Vibration ONLY SILENT";
			break;
		default:
			virbTypeStr = "What the hell?";
		    break;			    	
		}
		return virbTypeStr;
    }
    private String getRingerString(int type) {
    	String ringerTypeStr;
    	switch(type) {
    	case AudioManager.RINGER_MODE_NORMAL:
    		ringerTypeStr = "Ringer NORMAL";
    		break;
    	case AudioManager.RINGER_MODE_SILENT:
    		ringerTypeStr = "Ringer SILENT";
    		break;
    	case AudioManager.RINGER_MODE_VIBRATE:
    		ringerTypeStr = "Ringer VIBRATE";
    		break;
    	default:
    		ringerTypeStr = "What the hell?";
    		break;
    	}
    	return ringerTypeStr;
    }
    
    private void ToogleVibrationMode() {
    	int virbType = mineAudioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION);
		int virbMode;
		String virbTypeStr;
		switch (virbType) {
		case AudioManager.VIBRATE_SETTING_OFF:
			virbTypeStr = "Vibration OFF";
			virbMode = 0;
			break;
		case AudioManager.VIBRATE_SETTING_ON:
			virbTypeStr = "Vibration ON";
			virbMode = 1;
			break;
		case AudioManager.VIBRATE_SETTING_ONLY_SILENT:
			virbTypeStr = "Vibration ONLY SILENT";
			virbMode = 2;
			break;
		default:
			virbTypeStr = "What the hell?";
		    virbMode = 0;
		    break;
		}
		while (mainListAdapter.getCount() > 1)
		    mainListAdapter.remove(mainListAdapter.getItem(mainListAdapter.getCount()-1));
		mainListAdapter.add("Previous mode: " + virbTypeStr);
		mineAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, 
				VibrationModes[(++virbMode)%3]);
		virbTypeStr = getVibrationString(VibrationModes[virbMode%3]);
		mainListAdapter.add("Set to: " + virbTypeStr);
    }
    
    private void ToogleRingerMode() {
    	int ringerType = mineAudioManager.getRingerMode();
		int ringerMode;
		String ringerTypeStr;
		switch (ringerType) {
		case AudioManager.RINGER_MODE_NORMAL:
			ringerTypeStr = "Ringer NORMAL";
			ringerMode = 0;
			break;
		case AudioManager.RINGER_MODE_SILENT:
			ringerTypeStr = "Ringer SILENT";
			ringerMode = 1;
			break;
		case AudioManager.RINGER_MODE_VIBRATE:
			ringerTypeStr = "Ringer VIBRATE";
			ringerMode = 2;
			break;
		default:
			ringerTypeStr = "What the hell?";
			ringerMode = 0;
		    break;
		}
		while (mainListAdapter.getCount() > 1)
		    mainListAdapter.remove(mainListAdapter.getItem(mainListAdapter.getCount()-1));
		mainListAdapter.add("Previous mode: " + ringerTypeStr);
		mineAudioManager.setRingerMode( RingerModes[(++ringerMode)%3]);
		ringerTypeStr = getRingerString(RingerModes[ringerMode%3]);
		mainListAdapter.add("Set to: " + ringerTypeStr);
    }
    
    private void EnableMessageChecker() {
		MineLog.v("Enable Message Checker");
		MineVibrationToggler.EnableMessageVibration(mineContext, true);
    }
    private void DisableMessageChecker() {
		MineLog.v("Disable Message Checker");
		MineVibrationToggler.EnableMessageVibration(mineContext, false);
    }
    
    private void ToogleMessageChecker() {
    	
		while (mainListAdapter.getCount() > 1)
		    mainListAdapter.remove(mainListAdapter.getItem(mainListAdapter.getCount()-1));
		mainListAdapter.add("Message Vibration: " + 
				(messageCheckerEnabled?"ON":"OFF"));
		
    	if (messageCheckerEnabled) {
    		EnableMessageChecker();
    		messageCheckerEnabled = false;
    	} else {
    		DisableMessageChecker();
    		messageCheckerEnabled = true;
    	}
    }
}