package com.yang.backup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        //if (TextUtils.equals(action, IntentConst.Action.matchRemind)) {
            //通知比赛开始
        //    long matchID = intent.getLongExtra(Net.Param.ID, 0);
        //    showNotificationRemindMe(context, matchID);
        //}
    }
}
