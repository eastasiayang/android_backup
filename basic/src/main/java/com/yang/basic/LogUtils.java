/* Copyright (C) 2016 Tcl Corporation Limited */
package com.yang.basic;

import android.util.Log;

public class LogUtils {

    private static final boolean DEBUG = true;

    private static final String TAG = "Backup";

    public static void v(String tag, String msg) {
        if (DEBUG) {
            Log.v(TAG, "[" + tag + "] " + msg);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(TAG, "[" + tag + "] " + msg);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(TAG, "[" + tag + "] " + msg);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            Log.w(TAG, "[" + tag + "] " + msg);
        }
    }


    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(TAG, "[" + tag + "] " + msg);
        }
    }

    public static void t() {
        Log.e(TAG,Log.getStackTraceString(new Throwable())); 
    }
    
}
