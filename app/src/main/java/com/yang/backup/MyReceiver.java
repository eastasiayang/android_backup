package com.yang.backup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.yang.basic.LogUtils;

import java.util.Calendar;

public class MyReceiver extends BroadcastReceiver {
    private final String TAG = "MyReceiver";
    MyNotificationManager notify;

    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        LogUtils.d(TAG, action);
        if (TextUtils.equals(action, BackupConst.ParamsConst.NOTIFICATION)) {
            int id = intent.getIntExtra(BackupConst.ParamsConst.ID, 0);
            String title = intent.getStringExtra(BackupConst.ParamsConst.TITLE);
            String message = intent.getStringExtra(BackupConst.ParamsConst.MESSAGE);
            LogUtils.d(TAG, "title = " + title);
            LogUtils.d(TAG, "message = " + message);
            notify = new MyNotificationManager(context, id);
            notify.setMessage(title, message);
            notify.showNormal();
        }
    }
}
