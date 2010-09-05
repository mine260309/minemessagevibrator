package com.mine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.view.*;

public class MineVibrationSetting extends Activity {
	
	private ListView mainSettingListView;
	private ArrayAdapter<String> mainListAdapter;
	private Context mineContext;
	
	private String ToogleVibrationSetting = "Toogle Vibration";
	
	private boolean messageCheckerEnabled = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mineContext = this;
        mainSettingListView = new ListView(this);
        
        mainListAdapter = new ArrayAdapter<String>(this, R.layout.textview);
        mainSettingListView.setAdapter(mainListAdapter);
    	mainListAdapter.add(ToogleVibrationSetting);
    	
    	mainSettingListView.setOnItemClickListener(new OnItemClickListener() {
			    public void onItemClick(AdapterView<?> parent, View view,
			        int position, long id) {
			        String menuItem = ((TextView) view).getText().toString();
			    	if (menuItem == ToogleVibrationSetting) {
			    		ToogleMessageChecker();
			    	}
			    }
			  });
    	initVibrationmode();
		setContentView(mainSettingListView);		
    }
    
    private void initVibrationmode() {
    	messageCheckerEnabled = MineVibrationToggler.GetVibrationMode(mineContext);
    	if (messageCheckerEnabled) {
    		EnableMessageChecker();
    	}
    	else {
    		DisableMessageChecker();
    	}
		mainListAdapter.add("Message Vibration: " + 
				(messageCheckerEnabled?"ON":"OFF"));
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
    	
    	if (messageCheckerEnabled) {
    		messageCheckerEnabled = false;
    		DisableMessageChecker();
    	} else {
    		messageCheckerEnabled = true;
    		EnableMessageChecker();
    	}
    	
    	// Set the mode in the screen
		while (mainListAdapter.getCount() > 1) {
		    mainListAdapter.remove(mainListAdapter.getItem(mainListAdapter.getCount()-1));
		}
		mainListAdapter.add("Message Vibration: " + 
				(messageCheckerEnabled?"ON":"OFF"));
		
		Intent intent = new Intent(MineVibrationToggler.VIBRATION_ACTION_NAME);
		mineContext.sendBroadcast(intent);
    }
}