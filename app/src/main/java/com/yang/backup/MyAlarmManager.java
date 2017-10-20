package com.yang.backup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class MyAlarmManager {
    private static final String TAG = "MyAlarmManager";
    Context mContext;
    AlarmManager mAlarmManager;
    PendingIntent mPending;

    public MyAlarmManager(Context c) {
        mContext = c;
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    }

    public void setAlarm(int id, String title, String message, Calendar cal){
        Intent intent = new Intent(BackupConst.ParamsConst.NOTIFICATION);
        intent.putExtra(BackupConst.ParamsConst.ID, id);
        intent.putExtra(BackupConst.ParamsConst.TITLE, title);
        intent.putExtra(BackupConst.ParamsConst.MESSAGE, message);
        intent.setClass(mContext, MyReceiver.class);
        mPending = PendingIntent.getBroadcast(mContext, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), mPending);
    }
}
