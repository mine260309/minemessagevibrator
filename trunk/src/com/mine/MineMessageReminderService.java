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

public class MineMessageReminderService extends Service {

	private static final Object mStartingServiceSync = new Object();
	public static final String ACTION_REMIND_SET = "com.mine.ACTION_REMIND_SET";
	public static final String ACTION_REMIND_CANCEL = "com.mine.ACTION_REMIND_CANCEL";
	public static final String EXTRA_UNREAD_NUMBER = "com.mine.UNREAD";
	public static final String EXTRA_REMINDER_TYPE = "com.mine.REMINDER_TYPE";

	private static PowerManager.WakeLock mStartingService;

	private Context context;
	private MineReminderServiceHandler mServiceHandler;
	private Looper mServiceLooper;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		MineLog.v("MineMessageReminderService.onCreate()");
		HandlerThread thread = new HandlerThread(MineLog.LOGTAG,
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		context = getApplicationContext();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new MineReminderServiceHandler(mServiceLooper);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		MineLog.v("MineMessageReminderService.onStart()");

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
		MineLog.v("MineMessageReminderService.onDestroy()");
		mServiceLooper.quit();
	}

	/**
	 * Start the service to process the current event notifications, acquiring
	 * the wake lock before returning to ensure that the service will run.
	 */
	public static void beginStartingService(Context context, Intent intent) {
		synchronized (mStartingServiceSync) {
			MineLog.v("MineMessageReminderService.beginStartingService()");
			if (mStartingService == null) {
				PowerManager pm = (PowerManager) context
						.getSystemService(Context.POWER_SERVICE);
				mStartingService = pm.newWakeLock(
						PowerManager.PARTIAL_WAKE_LOCK, MineLog.LOGTAG
								+ ".MessageReminderService");
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
			MineLog.v("MineMessageReminderService.finishStartingService()");
			if (mStartingService != null) {
				if (service.stopSelfResult(startId)) {
					mStartingService.release();
				}
			}
		}
	}

	private class MineReminderServiceHandler extends Handler {
		public MineReminderServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			MineLog.v("MineMessageReminderService: handleMessage()");
			
			if (msg == null) {
				MineLog.e("receive null msg");
				return;
			}
			Intent intent = (Intent) msg.obj;
			if (intent == null) {
				return;
			}
			
			int serviceId = msg.arg1;
			String action = intent.getAction();

			if (ACTION_REMIND_SET.equals(action)) {
				processReminder(intent);
			}
			else if (ACTION_REMIND_CANCEL.equals(action)) {
				MineLog.v("User cancel reminder");
				MineMessageReminderReceiver.cancelReminder(context,
						MineMessageReminderReceiver.REMINDER_TYPE_WHATEVER);
			}

			// NOTE: We MUST not call stopSelf() directly, since we need to
			// make sure the wake lock acquired by AlertReceiver is released.
			finishStartingService(MineMessageReminderService.this, serviceId);
		}

		private void processReminder(Intent intent) {
			MineLog.v("processReminder");
			// TODO: need to find a way to determine if we need to notify
			// If user already read the SMS, we shall not notify;
			// So maybe it shall monitor the number of unread messages...
			// 
			// The logic:
			// If reminder is enabled
			//   if type is WHATEVER
			//     Should NOT happen!
			//     set type to contain ALL types
			//   fi
			//   if type contains REMINDER_TYPE_MESSAGE
			//     get number of unread messages (nUM)
			//     if nUM is not decreasing
			//       notify message and schedule new reminder
			//       return
			//     else
			//       cancel message reminder
			//     fi
			//   fi
			//   if type contains REMINDER_TYPE_PHONECALL
			//     and missedPhoneCalls reminder is enabled
			//     get the number of missed calls (nMC)
			//     if nMC is not 0
			//       notify missed calls and schedule new reminder
			//       return
			//     else
			//       cancel missed call reminder
			//     fi
			//   fi
			//   if type contains REMINDER_TYPE_GMAIL
			//     and unreadGmail reminder is enabled
			//     get the number of unread gmails (nGM)
			//     if nGM is not decreasing
			//       notify gmail and schedule new reminder
			//       return
			//     else
			//       cancel gmail reminder
			//     fi
			//   fi
			// Else
			//   cancel reminder
			// Fi
			
			int type = intent.getIntExtra(EXTRA_REMINDER_TYPE, 
					MineMessageReminderReceiver.REMINDER_TYPE_WHATEVER);

			if (MineVibrationToggler.GetReminderEnabled(context)) {
				MineLog.v("Reminder type: " + type);

				if (type == MineMessageReminderReceiver.REMINDER_TYPE_WHATEVER) {
					MineLog.e("Reminder type WHATEVER, should NOT happen!");
					type = MineMessageReminderReceiver.REMINDER_TYPE_MESSAGE
						| MineMessageReminderReceiver.REMINDER_TYPE_PHONECALL
						| MineMessageReminderReceiver.REMINDER_TYPE_GMAIL;
				}
				if ((type & MineMessageReminderReceiver.REMINDER_TYPE_MESSAGE)
						!= 0){
					int previousUnreadNumber = 0;
					int currentUnreadNumber = 0;
					currentUnreadNumber = MineMessageUtils.getUnreadMessagesCount(context);

					previousUnreadNumber = intent.getIntExtra(EXTRA_UNREAD_NUMBER, -1);
					if (previousUnreadNumber == -1) {
						MineLog.v("No unread number from intent!");
						previousUnreadNumber = 1; // set to 1 to simulate we have 1
													// unread previously
					}
					boolean isUnreadMessageDecreasing = currentUnreadNumber < previousUnreadNumber;
					if (!isUnreadMessageDecreasing) {
						MineMessageVibrator.notifyReminder(context,
								MineMessageReminderReceiver.REMINDER_TYPE_MESSAGE);
						MineMessageReminderReceiver.scheduleReminder(context,
								currentUnreadNumber,
								MineMessageReminderReceiver.REMINDER_TYPE_MESSAGE);
						return;
					} else {
						MineMessageReminderReceiver.cancelReminder(context,
								MineMessageReminderReceiver.REMINDER_TYPE_MESSAGE);
					}
				}
				if ((type & MineMessageReminderReceiver.REMINDER_TYPE_PHONECALL)
						!= 0){
					int missedPhoneCalls = 0;
					if (MineVibrationToggler.GetMissedPhoneCallReminderEnabled(context)) {
						missedPhoneCalls = MineMessageUtils.getMissedPhoneCalls(context);
					}
					if(missedPhoneCalls > 0) {
						MineMessageVibrator.notifyReminder(context,
								MineMessageReminderReceiver.REMINDER_TYPE_PHONECALL);
						MineMessageReminderReceiver.scheduleReminder(context,
								0,
								MineMessageReminderReceiver.REMINDER_TYPE_PHONECALL);
						return;
					} else {
						type &= (~MineMessageReminderReceiver.REMINDER_TYPE_PHONECALL);
						MineMessageReminderReceiver.cancelReminder(context,
								MineMessageReminderReceiver.REMINDER_TYPE_PHONECALL);
					}
				}
				if ((type & MineMessageReminderReceiver.REMINDER_TYPE_GMAIL)
						!= 0){
					int previousGmailNumber = 0;
					int unreadGmails = 0;
					boolean isUnreadGmailDecreasing = true;
					if (MineVibrationToggler.GetUnreadGmailReminderEnabled(context)) {
						unreadGmails = MineMessageUtils.getUnreadGmails(context);
						// assume there were unread gmails
						previousGmailNumber = MineVibrationToggler.getPreviousUnreadGmailNumber(context);
						if (previousGmailNumber == 0) {
							MineLog.e("previous unread gmail is 0?");
							previousGmailNumber = 1;
						}
						isUnreadGmailDecreasing = unreadGmails < previousGmailNumber;
						// save the unread count again
						MineVibrationToggler.savePreviousUnreadGmailNumber(context, unreadGmails);
					}
					if (!isUnreadGmailDecreasing) {
						MineMessageVibrator.notifyReminder(context,
								MineMessageReminderReceiver.REMINDER_TYPE_GMAIL);
						MineMessageReminderReceiver.scheduleReminder(context,
								0,
								MineMessageReminderReceiver.REMINDER_TYPE_GMAIL);
						return;
					} else {
						MineMessageReminderReceiver.cancelReminder(context,
								MineMessageReminderReceiver.REMINDER_TYPE_GMAIL);
					}
				}
			}
			else {
				MineMessageReminderReceiver.cancelReminder(context,
						MineMessageReminderReceiver.REMINDER_TYPE_WHATEVER);
			}
		}
	}
}
