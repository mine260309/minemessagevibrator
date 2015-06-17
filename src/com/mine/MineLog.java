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

public class MineLog {
  public final static String LOGTAG = "MineVibration";

  public static final boolean DEBUG = true;

  public static void v(String msg) {
    if (DEBUG)
      android.util.Log.v(LOGTAG, msg);
  }

  public static void e(String msg) {
    android.util.Log.e(LOGTAG, msg);
  }
}
