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
	public static Context getContext() {
		return context;
	}
}
