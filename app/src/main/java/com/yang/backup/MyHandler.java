package com.yang.backup;

import android.os.Handler;
import android.os.Message;

import com.yang.basic.LogUtils;

class MyHandler extends Handler {
    private static final String TAG = "MyHandler";
    final int UPDATE_MENU = 0;
    final int UPDATE_DELAY_TIMES = 1000;

    private HandlerCallback callback;

    public MyHandler(HandlerCallback callback){
        this.callback = callback;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        try {
            switch (msg.what) {
                case UPDATE_MENU:
                    LogUtils.d(TAG, "UPDATE_MENU");
                    callback.handle();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface HandlerCallback {
        void handle();
    }
}
