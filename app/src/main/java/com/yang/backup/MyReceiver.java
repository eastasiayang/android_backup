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
        if (TextUtils.equals(action, BackupConst.ParamsConst.NOTIFICATION)) {
            int id = intent.getIntExtra(BackupConst.ParamsConst.ID, 0);
            String str1 = intent.getStringExtra(BackupConst.ParamsConst.STRING1);
            String str2 = intent.getStringExtra(BackupConst.ParamsConst.STRING2);
            notify = new MyNotificationManager(context, id);
            notify.setMessage(str1, str2);
            notify.showNormal();
        }
    }
}
