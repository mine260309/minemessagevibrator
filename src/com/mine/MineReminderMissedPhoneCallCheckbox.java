package com.mine;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

public class MineReminderMissedPhoneCallCheckbox extends CheckBoxPreference {
	private Context context;
	private static String KEY_REMINDER_ITEM_MISSED_CALL = "reminderItemMissedCall";
	private static String KEY_REMINDER_ITEM_UNREAD_GMAIL = "reminderItemUnreadGmail";
	
	public MineReminderMissedPhoneCallCheckbox(Context c) {
		super(c);
		context = c;
	}

	public MineReminderMissedPhoneCallCheckbox(Context c,
			AttributeSet attrs) {
		super(c, attrs);
		context = c;
	}

	public MineReminderMissedPhoneCallCheckbox(Context c,
			AttributeSet attrs, int defStyle) {
		super(c, attrs, defStyle);
		context = c;
	}

	@Override
	protected void onClick() {
		super.onClick();
		String key = getKey();
		if (isChecked()) {
			if (KEY_REMINDER_ITEM_MISSED_CALL.equals(key)) {
				MineTelephonyListenService.startTelephonyListener(context);
			}
			else if (KEY_REMINDER_ITEM_UNREAD_GMAIL.equals(key)) {
				MineTelephonyListenService.startGmailWatcher(context);
			}
		}
		else {
			if (KEY_REMINDER_ITEM_MISSED_CALL.equals(key)) {
				MineTelephonyListenService.stopTelephonyListener(context);
			}
			else if (KEY_REMINDER_ITEM_UNREAD_GMAIL.equals(key)) {
				MineTelephonyListenService.stopGmailWatcher(context);
			}
		}
	}
}
