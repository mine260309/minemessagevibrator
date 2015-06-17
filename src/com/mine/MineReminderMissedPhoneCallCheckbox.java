/**
 * **********************************************************************
 * MineMessageVibrator is an Android App that provides vibrate and
 * reminder functions for SMS, MMS, Gmail, etc.
 * Copyright (C) 2010  Lei YU
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * **********************************************************************
 */

package com.mine;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

public class MineReminderMissedPhoneCallCheckbox extends CheckBoxPreference {
  private static String KEY_REMINDER_ITEM_MISSED_CALL = "reminderItemMissedCall";
  private static String KEY_REMINDER_ITEM_UNREAD_GMAIL = "reminderItemUnreadGmail";
  private Context context;

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

  private boolean isTokenValid(String[] token) {
    return !(token[0].equals("") || token[1].equals(""));
  }

  @Override
  protected void onClick() {
    super.onClick();
    String key = getKey();
    if (isChecked()) {
      if (KEY_REMINDER_ITEM_MISSED_CALL.equals(key)) {
        MineTelephonyListenService.startTelephonyListener(context);
      } else if (KEY_REMINDER_ITEM_UNREAD_GMAIL.equals(key)) {
        MineLog.v("reminder gmail clicked");
        String[] token = MineMessageUtils.getGmailToken(context);
        if (isTokenValid(token)) {
          setChecked(true);
          MineTelephonyListenService.startGmailWatcher(context);
        } else {
          setChecked(false);
        }
        MineLog.v("get token: " + token);
      }
    } else {
      if (KEY_REMINDER_ITEM_MISSED_CALL.equals(key)) {
        MineTelephonyListenService.stopTelephonyListener(context);
        // Test only, remove the gmail token
        // MineLog.v("remove gmail token");
        // MineVibrationToggler.removeGmailToken(context);
      } else if (KEY_REMINDER_ITEM_UNREAD_GMAIL.equals(key)) {
        MineTelephonyListenService.stopGmailWatcher(context);
        MineLog.v("reminder gmail un-clicked");
      }
    }
  }
}
