package com.mine;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MineMessageVibratorWidget extends AppWidgetProvider {

	private static final int BUTTON_TOGGLE_VIBRATE = 0;
	private static final ComponentName THIS_WIDGET = new ComponentName(
			"com.mine", "com.mine.MineMessageVibratorWidget");

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		RemoteViews view = makeVibrateView(context, -1);

		for (int i = 0; i < appWidgetIds.length; i++) {
			appWidgetManager.updateAppWidget(appWidgetIds[i], view);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) {
			Uri data = intent.getData();
			int buttonId = Integer.parseInt(data.getSchemeSpecificPart());

			if (buttonId == BUTTON_TOGGLE_VIBRATE) {
				toggleVibration(context);
			}
		}

		updateWidget(context);
	}

	@Override
	public void onEnabled(Context context) {
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(new ComponentName("com.mine",
				".MineMessageVibratorWidget"),
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP);
	}

	@Override
	public void onDisabled(Context context) {
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(new ComponentName("com.mine",
				".MineMessageVibratorWidget"),
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);
	}

	private void toggleVibration(Context context) {
		MineLog.v("Toggle vibration");

		// Get setting from preference
		boolean vibratePref = MineVibrationToggler
				.GetVibrationEnabledPreference(context);
		boolean vibAppAutoEnabled = MineVibrationToggler
				.GetAppAutoEnabled(context);
		/**
		 * if AppAuto case1 vib ON, auto ON: this should not happen; case2 vib
		 * ON, auto OFF: this is the normal case, just toggle it; case3 vib OFF,
		 * auto ON: toggle auto to OFF; case4 vib OFF, auto OFF: this is the
		 * normal case, just toggle it; else just normal case
		 * 
		 * so if auto ON, disable auto; if auto OFF, just normal case;
		 */
		String info;
		if (vibAppAutoEnabled) {
			MineVibrationToggler.DisableAppAuto(context);
			info = context.getString(R.string.app_auto_disable);
			Toast.makeText(context, info, Toast.LENGTH_SHORT).show();
		} else {
			vibratePref = vibratePref ? false : true;
			MineVibrationToggler.EnableMessageVibration(context, vibratePref);

			if (vibratePref) {
				info = context.getString(R.string.enable_vibration_info);
			} else {
				info = context.getString(R.string.disable_vibration_info);
			}
			Toast.makeText(context, info, Toast.LENGTH_SHORT).show();
		}
	}

	public static void updateWidget(Context context) {
		MineLog.v("updateWidget...");
		RemoteViews views = makeVibrateView(context, -1);
		final AppWidgetManager gm = AppWidgetManager.getInstance(context);
		gm.updateAppWidget(THIS_WIDGET, views);
	}

	private static RemoteViews makeVibrateView(Context context, int appWidgetId) {
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widgetlayout);

		views.setOnClickPendingIntent(R.id.btn_toggle_vibrate,
				getLaunchPendingIntent(context, appWidgetId,
						BUTTON_TOGGLE_VIBRATE));

		updateButtons(views, context);
		return views;
	}

	private static void updateButtons(RemoteViews views, Context context) {
		boolean vibrate_enabled = MineVibrationToggler
				.GetVibrationEnabled(context);
		if (vibrate_enabled) {
			views.setImageViewResource(R.id.btn_toggle_vibrate,
					R.drawable.mes_button_vibrate);
		} else {
			views.setImageViewResource(R.id.btn_toggle_vibrate,
					R.drawable.mes_button_no_vibrate);
		}
	}

	private static PendingIntent getLaunchPendingIntent(Context context,
			int appWidgetId, int buttonId) {
		Intent launchIntent = new Intent();
		launchIntent.setClass(context, MineMessageVibratorWidget.class);
		launchIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		launchIntent.setData(Uri.parse("custom:" + buttonId));

		PendingIntent pi = PendingIntent.getBroadcast(context, 0, launchIntent,
				0);

		return pi;
	}
}