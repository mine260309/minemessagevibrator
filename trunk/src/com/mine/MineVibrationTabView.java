package com.mine;

import com.admob.android.ads.AdManager;
import com.admob.android.ads.AdView;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class MineVibrationTabView extends TabActivity {

	private static MineVibrationTabView context;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.tablayout);

		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		// Create an Intent for the regular live wallpaper preferences activity
		intent = new Intent().setClass(this, MineVibrationSetting.class);

		// Initialize a TabSpec and set the intent
		spec = tabHost.newTabSpec("settings").setContent(intent);
		spec.setIndicator("Settings");

		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		//AdManager.setTestDevices( new String[] {AdManager.TEST_EMULATOR} ); 
		AdView adview = (AdView)findViewById(R.id.ad);
		adview.requestFreshAd();
	}
	
	public static Context getContext() {
		return context;
	}
}
