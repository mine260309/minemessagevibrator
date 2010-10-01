package com.mine;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MineVibrationSetting extends PreferenceActivity {
	
	private static final int FIRST_TIME_RUN_DIALOG_ID = 1;
	private static final int UPGRADED_RUN_DIALOG_ID = 2;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(MineVibrationToggler.IsFirstRun(this)) {
        	// This is the first time running, show a help screen
        	showDialog(FIRST_TIME_RUN_DIALOG_ID);
        }
        if(MineVibrationToggler.IsUpgraded(this)){
        	showDialog(UPGRADED_RUN_DIALOG_ID);
        }
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
    	// show dialog according to the id
    	if (id == FIRST_TIME_RUN_DIALOG_ID ) {
	    	return new AlertDialog.Builder(this)
	    	.setMessage(getString(R.string.first_run_dialog_message))
	    	.setTitle(getString(R.string.first_run_dialog_title))
	    	.setPositiveButton(R.string.OK_string, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	                /* User clicked OK so do some stuff */
	            }
	        })
	        .create();
    	}
    	else {
	    	return new AlertDialog.Builder(this)
	    	.setMessage(getString(R.string.upgraded_run_dialog_message))
	    	.setTitle(getString(R.string.upgraded_run_dialog_title))
	    	.setPositiveButton(R.string.OK_string, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	                /* User clicked OK so do some stuff */
	            }
	        })
	        .create();
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.addSubMenu(0, FIRST_TIME_RUN_DIALOG_ID, 0, R.string.menu_help_string);
        menu.addSubMenu(0, UPGRADED_RUN_DIALOG_ID, 0, R.string.menu_whatsnew_string);

        return result;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId())
    	{
    	case FIRST_TIME_RUN_DIALOG_ID:
    		showDialog(FIRST_TIME_RUN_DIALOG_ID);
    		break;
    	case UPGRADED_RUN_DIALOG_ID:
    		showDialog(UPGRADED_RUN_DIALOG_ID);
    		break;
    	}
        return super.onOptionsItemSelected(item);
    }
}