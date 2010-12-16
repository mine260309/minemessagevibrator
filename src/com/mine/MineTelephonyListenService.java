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
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class MineTelephonyListenService extends Service {
	private static final Object mStartingServiceSync = new Object();
	private static PowerManager.WakeLock mStartingService;
	public static final String ACTION_START_TELEPHONY_LISTEN = "com.mine.START_TELEPHONY_LISTEN";

	private Context context;
	private MineTelephonyListenServiceHandler mServiceHandler;
	private Looper mServiceLooper;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
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
	}

	/**
	 * Start the service to process the current event notifications, acquiring
	 * the wake lock before returning to ensure that the service will run.
	 */
	public static void beginStartingService(Context context, Intent intent) {
		synchronized (mStartingServiceSync) {
			MineLog.v("MineTelephonyListenService.beginStartingService()");
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
			MineLog.v("MineTelephonyListenService.finishStartingService()");
			if (mStartingService != null) {
				if (service.stopSelfResult(startId)) {
					mStartingService.release();
				}
			}
		}
	}

	private final class MineTelephonyListenServiceHandler extends Handler {
		public MineTelephonyListenServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			MineLog.v("MineTelephonyListenServiceHandler: handleMessage()");
			int serviceId = msg.arg1;
			Intent intent = (Intent) msg.obj;
			String action = intent.getAction();

			if (action.equals(ACTION_START_TELEPHONY_LISTEN)) {
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				tm.listen(phoneStateListener,
						PhoneStateListener.LISTEN_CALL_STATE);
				MineLog.v("set telephony listener......");
			}
			// NOTE: We MUST not call stopSelf() directly, since we need to
			// make sure the wake lock acquired by AlertReceiver is released.
			finishStartingService(MineTelephonyListenService.this, serviceId);
		}
	}

	private final PhoneStateListener phoneStateListener = new PhoneStateListener() {
		public void onCallStateChanged(int state, String incomingNumber) {
			MineLog.v("onCallStateChanged: " + state + ", number: "
					+ incomingNumber);
		}
	};
}
