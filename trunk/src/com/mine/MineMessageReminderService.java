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
	public static final String ACTION_REMIND = "com.mine.ACTION_REMIND";
	public static final String UNREAD_NUMBER = "com.mine.UNREAD";
	
	private static PowerManager.WakeLock mStartingService;
	
	private Context context;
	private MineReminderServiceHandler mServiceHandler;
	private Looper mServiceLooper;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		MineLog.v("MineMessageReminderService.onCreate()");
	    HandlerThread thread = new HandlerThread(MineLog.LOGTAG, Process.THREAD_PRIORITY_BACKGROUND);
	    thread.start();
	    context = getApplicationContext();
	    mServiceLooper = thread.getLooper();
	    mServiceHandler = new MineReminderServiceHandler(mServiceLooper);
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
	  MineLog.v("MineMessageReminderService.onStart()");

	  Message msg = mServiceHandler.obtainMessage();
	  msg.arg1 = startId;
	  msg.obj = intent;
	  mServiceHandler.sendMessage(msg);
	}

	@Override
	public void onDestroy() {
	    MineLog.v("MineMessageReminderService.onDestroy()");
	    mServiceLooper.quit();
	}
	
	/**
	 * Start the service to process the current event notifications, acquiring the
	 * wake lock before returning to ensure that the service will run.
	 */
	public static void beginStartingService(Context context, Intent intent) {
	  synchronized (mStartingServiceSync) {
	    MineLog.v("MineMessageReminderService.beginStartingService()");
	    if (mStartingService == null) {
	        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
	        mStartingService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
	              MineLog.LOGTAG+".MessageReminderService");
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

	        int serviceId = msg.arg1;
	        Intent intent = (Intent) msg.obj;
	        String action = intent.getAction();

	        if (ACTION_REMIND.equals(action)) {
	        	processReminder(intent);
	        }/* else if (Intent.ACTION_SCREEN_ON.equals(action)) {
	        	MineMessageReminderReceiver.cancelReminder(context);
	        }*/

	        // NOTE: We MUST not call stopSelf() directly, since we need to
	        // make sure the wake lock acquired by AlertReceiver is released.
	        finishStartingService(MineMessageReminderService.this, serviceId);
	    }

	    private void processReminder(Intent intent) {
			MineLog.v("processReminder");
			// TODO: need to find a way to determine if we need to notify
			// If user already read the SMS, we shall not notify;
			// So maybe it shall monitor the number of unread messages...
			// Case 1: unread message number is decreasing before the previous schedule
			//			-> cancel notify;
			// Case 2: unread message number is increasing, maybe this it not going to happen
			//			-> keep reminding;
			// Case 3: unread message number is not changing, 
			//			-> keep reminding;
			
			int previousUnreadNumber = intent.getIntExtra(UNREAD_NUMBER, -1);
			if (previousUnreadNumber == -1) {
				MineLog.e("Error getting unread number from intent!");
				previousUnreadNumber = 1; // set to 1 to simulate we have 1 unread previously
			}
			int currentUnreadNumber = MineMessageUtils.getUnreadMessagesCount(context);
			
			//TODO: maybe I can optimize the code
			if (currentUnreadNumber < previousUnreadNumber) {
				MineMessageReminderReceiver.cancelReminder(context);
			} else {
				if (MineVibrationToggler.GetReminderEnabled(context)) {
					// Check if reminder is still enabled or not
					MineMessageVibrator.notifyReminder(context);
					MineMessageReminderReceiver.scheduleReminder(context, currentUnreadNumber);
				}
				else {
					MineMessageReminderReceiver.cancelReminder(context);
				}
			}
	    }
	}
}
