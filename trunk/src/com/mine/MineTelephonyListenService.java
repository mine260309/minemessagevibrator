package com.mine;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class MineTelephonyListenService extends Service {
	private static final Object mStartingServiceSync = new Object();
	private static PowerManager.WakeLock mWakeLock;
	private static Uri UnreadGmailUri = Uri.parse("content://gmail-ls/unread/inbox");

	public static final String ACTION_START_TELEPHONY_LISTEN = "com.mine.START_TELEPHONY_LISTEN";
	public static final String ACTION_STOP_TELEPHONY_LISTEN = "com.mine.STOP_TELEPHONY_LISTEN";
	public static final String ACTION_START_GMAIL_WATCHER = "com.mine.START_GMAIL_WATCHER";
	public static final String ACTION_STOP_GMAIL_WATCHER = "com.mine.STOP_GMAIL_WATCHER";
	
	public static final String ACTION_INCOMING_CALL_RECEIVED = "com.mine.INCOMING_CALL_RECEIVED";
	public static final String ACTION_UNREAD_GMAIL_RECEIVED = "com.mine.UNREAD_GMAIL_RECEIVED";

	private Context context;
	private MineTelephonyListenServiceHandler mServiceHandler;
	private Looper mServiceLooper;
	private boolean telephonyListenerEnabled;
	private boolean gmailWatcherEnabled;
	
	private final IBinder binder = new ServiceBinder(this);
	public static class ServiceBinder extends Binder {
		
		private final MineTelephonyListenService service;
		
		public ServiceBinder(MineTelephonyListenService service) {
			this.service = service;
		}
		
		public MineTelephonyListenService getService() {
			return service;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		MineLog.v("MineTelephonyListenService.onCreate()");
		HandlerThread thread = new HandlerThread(MineLog.LOGTAG,
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		context = getApplicationContext();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new MineTelephonyListenServiceHandler(mServiceLooper);
		telephonyListenerEnabled = gmailWatcherEnabled = false;
		// if missed phone call reminder is enabled,
		// start telephony listener
		if (MineVibrationToggler.GetMissedPhoneCallReminderEnabled(context)) {
			TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
			tm.listen(phoneStateListener,
					PhoneStateListener.LISTEN_CALL_STATE);
			MineLog.v("Register telephony listener......");
			telephonyListenerEnabled = true;
		}
		if (MineVibrationToggler.GetUnreadGmailReminderEnabled(context)) {
			context.getContentResolver().
				registerContentObserver(UnreadGmailUri, true, gmailObserver);
			MineLog.v("Register gmail watcher......");
			gmailWatcherEnabled = true;
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		MineLog.v("MineTelephonyListenService.onStart()");

		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		msg.obj = intent;
		mServiceHandler.sendMessage(msg);
	}

	@Override
	public void onDestroy() {
		MineLog.v("MineTelephonyListenService.onDestroy()");
		mServiceLooper.quit();
		releaseLock();
	}

	public static void acquireLock(Context context) {
		synchronized (mStartingServiceSync) {
			if (mWakeLock == null) {
				PowerManager pm = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
				mWakeLock = pm.newWakeLock(
						PowerManager.PARTIAL_WAKE_LOCK, MineLog.LOGTAG
						+ ".TelephonyListenService");
				mWakeLock.setReferenceCounted(false);
			}
			mWakeLock.acquire();
			MineLog.v("acquire Wake Lock for TelephonyListenService");
		}
	}
	public static void releaseLock() {
		synchronized (mStartingServiceSync) {
			if (mWakeLock != null) {
				mWakeLock.release();
				mWakeLock = null;
				MineLog.v("release Wake Lock for TelephonyListenService");
			}
		}
	}

	private final class MineTelephonyListenServiceHandler extends Handler {
		public MineTelephonyListenServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			//int serviceId = msg.arg1;
			Intent intent = (Intent) msg.obj;
			String action = intent.getAction();
			MineLog.v("MineTelephonyListenServiceHandler: handleMessage() " 
					+ action);

			if (action.equals(ACTION_INCOMING_CALL_RECEIVED)) {
				if (MineVibrationToggler.GetReminderEnabled(context) &&
						MineVibrationToggler.GetMissedPhoneCallReminderEnabled(context)) {
					MineMessageReminderReceiver.scheduleReminder(context, 0,
						MineMessageReminderReceiver.REMINDER_TYPE_PHONECALL);
				}
			}
			else if (action.equals(ACTION_UNREAD_GMAIL_RECEIVED)) {
				if (MineVibrationToggler.GetReminderEnabled(context) &&
						MineVibrationToggler.GetUnreadGmailReminderEnabled(context)) {
					int unread = intent.getIntExtra("unread", 0);
					MineMessageReminderReceiver.scheduleReminder(context, unread,
						MineMessageReminderReceiver.REMINDER_TYPE_GMAIL);
				}
			}
			else if (action.equals(ACTION_STOP_TELEPHONY_LISTEN)) {
				TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
				tm.listen(phoneStateListener,
						PhoneStateListener.LISTEN_NONE);
				MineLog.v("Unregister telephony listener......");
				telephonyListenerEnabled = false;
			}
			else if (action.equals(ACTION_STOP_GMAIL_WATCHER)) {
				context.getContentResolver().unregisterContentObserver(gmailObserver);
				MineLog.v("Unregister gmail watcher......");
				gmailWatcherEnabled = false;
			}
			else if (action.equals(ACTION_START_TELEPHONY_LISTEN)) {
				if (!telephonyListenerEnabled) {
					TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
					tm.listen(phoneStateListener,
						PhoneStateListener.LISTEN_CALL_STATE);
					MineLog.v("Register telephony listener......");
					telephonyListenerEnabled = true;
				}
			}
			else if (action.equals(ACTION_START_GMAIL_WATCHER)) {
				if (!gmailWatcherEnabled) {
					context.getContentResolver().
						registerContentObserver(UnreadGmailUri, true, gmailObserver);
					MineLog.v("Register gmail watcher......");
					gmailWatcherEnabled = true;
				}
			}

			if (!telephonyListenerEnabled && !gmailWatcherEnabled) {
				// no listener is enabled, stop this service
				stopSelf();
			}
			releaseLock();
		}
	}

	private final PhoneStateListener phoneStateListener = new PhoneStateListener() {
		public void onCallStateChanged(int state, String incomingNumber) {
			MineLog.v("onCallStateChanged: " + state + ", number: "
					+ incomingNumber);
			if (state == TelephonyManager.CALL_STATE_RINGING){
				Intent intent = new Intent(ACTION_INCOMING_CALL_RECEIVED);
				intent.setClass(context, MineTelephonyListenService.class);
				MineTelephonyListenService.acquireLock(context);
				context.startService(intent);
			}
		}
	};
	
	private final Handler handler = new Handler();
    private final ContentObserver gmailObserver = new ContentObserver(handler) {
    	private int lastUnreadGmail = 0;
    	@Override
	    public void onChange(boolean selfChange) {
    		MineLog.v("GMail watcher: onChange");
    		int numUnread = MineMessageUtils.getUnreadGmails(context);
    		if (numUnread > 0) {
    			if (numUnread > lastUnreadGmail) {
    				MineMessageVibrator.notifyGmail(context);
        			Intent intent = new Intent(ACTION_UNREAD_GMAIL_RECEIVED);
    				intent.setClass(context, MineTelephonyListenService.class);
    				intent.putExtra("unread", numUnread);
    				MineTelephonyListenService.acquireLock(context);
    				context.startService(intent);
    			}
    			else {
    				MineLog.v("Unread gmail is decreasing, do nothing");
    			}
    		}
    		lastUnreadGmail = numUnread;
	    }
    };

	public static void startTelephonyListener(Context context) {
		Intent intent = new Intent(ACTION_START_TELEPHONY_LISTEN);
		Context appContext = MineVibrationTabView.getContext();
		if (appContext != null) {
			appContext.startService(intent);
			if (appContext.bindService(intent, (ServiceConnection) context, 0)) {
				((MineVibrationSetting)context).telephonyListenServiceBound = true;
			}
		}
		else {
			MineLog.v("appContext is null, start telephony listener only");
			context.startService(intent);
		}
	}
	
	public static void stopTelephonyListener(Context context) {
		Intent intent = new Intent(ACTION_STOP_TELEPHONY_LISTEN);
		
		Context appContext = MineVibrationTabView.getContext();
		if (appContext != null) {
			appContext.startService(intent);
			//appContext.sendBroadcast(intent);
			//intent.setClass(appContext, MineTelephonyListenService.class);
			//appContext.stopService(intent);
		}
		else {
			context.startService(intent);
			//intent.setClass(context, MineTelephonyListenService.class);
			//context.stopService(intent);
			MineLog.v("appContext is null, still stop telephony listener");
		}
	}
	
	public static void startGmailWatcher(Context context) {
		Intent intent = new Intent(ACTION_START_GMAIL_WATCHER);
		Context appContext = MineVibrationTabView.getContext();
		if (appContext != null) {
			appContext.startService(intent);
			if (appContext.bindService(intent, (ServiceConnection) context, 0)) {
				((MineVibrationSetting)context).telephonyListenServiceBound = true;
			}
		}
		else {
			MineLog.v("appContext is null, start gmail watcher only");
			context.startService(intent);
		}
	}
	
	public static void stopGmailWatcher(Context context) {
		Intent intent = new Intent(ACTION_STOP_GMAIL_WATCHER);
		
		Context appContext = MineVibrationTabView.getContext();
		if (appContext != null) {
			appContext.startService(intent);
		}
		else {
			context.startService(intent);
			MineLog.v("appContext is null, still stop gmail watcher");
		}
	}
}
