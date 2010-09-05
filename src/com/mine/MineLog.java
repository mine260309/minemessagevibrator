package com.mine;

public class MineLog {
	public final static String LOGTAG = "MineVibration";

	public static final boolean DEBUG = false;
	
	public static void v(String msg) {
		if(DEBUG)
	      android.util.Log.v(LOGTAG, msg);
	}

	public static void e(String msg) {
		android.util.Log.e(LOGTAG, msg);
	}
}