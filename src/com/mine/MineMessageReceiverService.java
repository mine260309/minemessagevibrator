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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;

public class MineMessageReceiverService extends Service {

	private static final Object mStartingServiceSync = new Object();
	private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private static final String ACTION_MMS_RECEIVED = "android.provider.Telephony.WAP_PUSH_RECEIVED";
	private static final String MMS_DATA_TYPE = "application/vnd.wap.mms-message";

	// Below parameter are extract from android mms app
	private static final String STATE = "state";
	private  static final int STATE_SUCCESS = 1;
	private static final int STATE_FAILED = 2;
	private static final String ACTION_MMS_TRANSACTION_COMPLETED = "android.intent.action.TRANSACTION_COMPLETED_ACTION";

	private static PowerManager.WakeLock mStartingService;

	private Context context;
	private MineMessageServiceHandler mServiceHandler;
	private Looper mServiceLooper;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		MineLog.v("MineMessageReceiverService.onCreate()");
		HandlerThread thread = new HandlerThread(MineLog.LOGTAG,
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		context = getApplicationContext();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new MineMessageServiceHandler(mServiceLooper);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		MineLog.v("MineMessageReceiverService.onStart()");

		Message msg = mServiceHandler.obtainMessage();
		if (msg != null) {
			msg.arg1 = startId;
			msg.obj = intent;
			mServiceHandler.sendMessage(msg);
		} else {
			MineLog.v("Error obtain message!");
		}
	}

	@Override
	public void onDestroy() {
		MineLog.v("MineMessageReceiverService.onDestroy()");
		mServiceLooper.quit();
	}

	/**
	 * Start the service to process the current event notifications, acquiring
	 * the wake lock before returning to ensure that the service will run.
	 */
	public static void beginStartingService(Context context, Intent intent) {
		synchronized (mStartingServiceSync) {
			MineLog.v("MineMessageReceiverService.beginStartingService()");
			if (mStartingService == null) {
				PowerManager pm = (PowerManager) context
						.getSystemService(Context.POWER_SERVICE);
				mStartingService = pm.newWakeLock(
						PowerManager.PARTIAL_WAKE_LOCK, MineLog.LOGTAG
								+ ".MessageReceiverService");
				mStartingService.setReferenceCounted(false);
			}
			mStartingService.acquire();

			context.startService(intent);
		}
	}

	/**
	 * Called back by the service when it has finished processing notifications,
	 * releasing the wake lock if the service is now stopping.
	 */
	public static void finishStartingService(Service service, int startId) {
		synchronized (mStartingServiceSync) {
			MineLog.v("MineMessageReceiverService.finishStartingService()");
			if (mStartingService != null) {
				if (service.stopSelfResult(startId)) {
					mStartingService.release();
				}
			}
		}
	}

	private final class MineMessageServiceHandler extends Handler {
		public MineMessageServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			MineLog.v("MineMessageReceiverService: handleMessage()");
			
			if (msg == null) {
				MineLog.e("receive null msg");
				return;
			}
			Intent intent = (Intent) msg.obj;
			if (intent == null) {
				return;
			}

			int serviceId = msg.arg1;;
			try {
				String action = intent.getAction();
				String dataType = intent.getType();

				if (ACTION_SMS_RECEIVED.equals(action)) {
					handleSmsReceived(intent);
				} else if (ACTION_MMS_RECEIVED.equals(action)
						&& MMS_DATA_TYPE.equals(dataType)) {
					//handleMmsReceived(intent);
					MineLog.v("Received a mms, probably downloading...");
				} else if (ACTION_MMS_TRANSACTION_COMPLETED.equals(action)) {
					MineLog.v("ACTION_MMS_TRANSACTION_COMPLETED");
					int state = intent.getIntExtra(STATE, STATE_FAILED);
					if (state == STATE_SUCCESS) {
						MineLog.v("TRANSACTION_COMPLETED and success");
						handleMmsReceived(intent);
					} else {
						MineLog.v("TRANSACTION_COMPLETED but not success");
					}
				}

				if (MineVibrationToggler.GetReminderEnabled(context)) {
					MineMessageReminderReceiver.scheduleReminder(context, -1,
							MineMessageReminderReceiver.REMINDER_TYPE_MESSAGE);
				}
			}
			catch (Exception ex) {
				MineLog.e("Exception: " + ex.toString());
			}

			// NOTE: We MUST not call stopSelf() directly, since we need to
			// make sure the wake lock acquired by AlertReceiver is released.
			finishStartingService(MineMessageReceiverService.this, serviceId);
		}

		private void handleSmsReceived(Intent intent) {
			MineLog.v("handling received sms");
			MineMessageVibrator.notifySMS(context);
			MineVibrationToggler.DimScreenForReceivedSMSIfNeeded(context);
		}

		private void handleMmsReceived(Intent intent) {
			MineLog.v("handling received mms");
			MineMessageVibrator.notifyMMS(context);
			MineVibrationToggler.DimScreenForReceivedSMSIfNeeded(context);
		}
	}

}
