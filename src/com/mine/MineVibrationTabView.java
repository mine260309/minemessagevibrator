package com.mine;

//import java.util.Timer;
//import java.util.TimerTask;

//import com.admob.android.ads.AdManager;
//import com.admob.android.ads.AdView;

import com.adwhirl.AdWhirlLayout;
//import com.adwhirl.AdWhirlTargeting;
import com.adwhirl.AdWhirlLayout.AdWhirlInterface;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
//import android.view.View;
import android.widget.TabHost;

public class MineVibrationTabView extends TabActivity implements AdWhirlInterface {

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
//		AdManager.setTestDevices( new String[] {AdManager.TEST_EMULATOR} );

/*
		Timer timer = new Timer();
		TimerTask tt = new TimerTask() {
			public void run() {
				AdManager.setTestDevices( new String[] {AdManager.TEST_EMULATOR} );
				AdView adview = (AdView)findViewById(R.id.ad);
				adview.requestFreshAd();
				MineLog.v("keywords: " + adview.getKeywords()+", hasAd: "+adview.hasAd());
				if( !adview.hasAd() ) {
					adview.setVisibility(View.VISIBLE);
				}
			}
		};
		timer.schedule(tt, 3000);
*/

	    AdWhirlLayout adWhirlLayout = (AdWhirlLayout) findViewById(R.id.adwhirl_layout);
	    adWhirlLayout.setAdWhirlInterface(this);
	    adWhirlLayout.setMaxWidth(320);
	    adWhirlLayout.setMaxHeight(52);
	}
	public static Context getContext() {
		return context;
	}
	@Override
	public void adWhirlGeneric() {
	    MineLog.e("In adWhirlGeneric()");
	}
}