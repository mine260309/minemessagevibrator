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
