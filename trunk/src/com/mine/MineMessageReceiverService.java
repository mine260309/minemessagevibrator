package com.mine;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

public class MineMessageReceiverService extends Service {

	private static final Object mStartingServiceSync = new Object();
	private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private static final String ACTION_MMS_RECEIVED = "android.provider.Telephony.WAP_PUSH_RECEIVED";
	private static final String MMS_DATA_TYPE = "application/vnd.wap.mms-message";
	  
	private Context context;
	private MineMessageServiceHandler mServiceHandler;
	private Looper mServiceLooper;
	  
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		MineLog.v("MineMessageReceiverService.onCreate()");
	    HandlerThread thread = new HandlerThread(MineLog.LOGTAG, Process.THREAD_PRIORITY_BACKGROUND);
	    thread.start();
	    context = getApplicationContext();
	    mServiceLooper = thread.getLooper();
	    mServiceHandler = new MineMessageServiceHandler(mServiceLooper);
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
	  MineLog.v("MineMessageReceiverService.onStart()");

	  Message msg = mServiceHandler.obtainMessage();
	  msg.arg1 = startId;
	  msg.obj = intent;
	  mServiceHandler.sendMessage(msg);
	}

	@Override
	public void onDestroy() {
	    MineLog.v("MineMessageReceiverService.onDestroy()");
	    mServiceLooper.quit();
	}
	
	  /**
	   * Start the service to process the current event notifications, acquiring the
	   * wake lock before returning to ensure that the service will run.
	   */
	  public static void beginStartingService(Context context, Intent intent) {
	    synchronized (mStartingServiceSync) {
	      MineLog.v("MineMessageReceiverService.beginStartingService()");
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
	      service.stopSelf(startId);
	    }
	  }
	  
	private final class MineMessageServiceHandler extends Handler {
		public MineMessageServiceHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
		  MineLog.v("MineMessageReceiverService: handleMessage()");

		  int serviceId = msg.arg1;
		  Intent intent = (Intent) msg.obj;
		  String action = intent.getAction();
		  String dataType = intent.getType();

		  if (ACTION_SMS_RECEIVED.equals(action)) {
			  handleSmsReceived(intent);
		  } else if (ACTION_MMS_RECEIVED.equals(action) && MMS_DATA_TYPE.equals(dataType)) {
		      handleMmsReceived(intent);
		  } 
		  MineMessageReminderReceiver.scheduleReminder(context, -1);
		  // NOTE: We MUST not call stopSelf() directly, since we need to
		  // make sure the wake lock acquired by AlertReceiver is released.
		  finishStartingService(MineMessageReceiverService.this, serviceId);
		}

		private void handleSmsReceived(Intent intent) {
			MineLog.v("handling received sms");
			MineMessageVibrator.notifySMS(context);
		}

		private void handleMmsReceived(Intent intent) {
			MineLog.v("handling received mms");
			MineMessageVibrator.notifyMMS(context);
		}
	}

}
