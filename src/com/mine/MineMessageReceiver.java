package com.mine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MineMessageReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		MineLog.v("Received a message");
		intent.setClass(context, MineMessageReceiverService.class);
		intent.putExtra("result", getResultCode());

		/*
		 * This service will process the activity and play notifications after
		 * it's work is done the service will be stopped.
		 */
		MineMessageReceiverService.beginStartingService(context, intent);
	}

}
