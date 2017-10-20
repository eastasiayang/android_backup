package com.yang.backup;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.yang.basic.LogUtils;
import com.yang.basic.MyCalendarHelp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class MyNotificationManager {
    private static final String TAG = "NotificationManager";
    private Context mContext;
    NotificationManager manager;
    private Bitmap icon;
    String m_title, m_message;
    int iID = 0;
    MyCalendarHelp m_CalHelp;

    public MyNotificationManager(Context c, int id){
        mContext = c;
        m_CalHelp = new MyCalendarHelp(mContext);
        manager = (NotificationManager) mContext.getSystemService
                (Context.NOTIFICATION_SERVICE);
        icon = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.ic_gps);
        iID = id;
        m_title = mContext.getResources().getString(R.string.no_title);
        m_message = mContext.getResources().getString(R.string.no_message);
    }

    public void setMessage(String title, String message)
    {
        m_title = title;
        m_message = message;
    }

    public void showNormal() {
        Notification notification = new NotificationCompat.Builder(mContext)
                .setLargeIcon(icon)
                .setSmallIcon(R.drawable.ic_date)
                .setTicker("showNormal")
                .setContentInfo("contentInfo")
                .setContentTitle(m_title)
                .setContentText(m_message)
                .setNumber(1)
                .setAutoCancel(true)
                .build();
        manager.notify(iID, notification);
    }

    public void SetNotification(Calendar C_now){
        String result;
        String now = m_CalHelp.CalendarToString(C_now, m_CalHelp.DATE_FORMAT_SQL);
        result = DataBaseManager.getInstance(mContext).getStartedRecordList(Calendar.getInstance());
        try {
            JSONObject obj = new JSONObject(result);
            if (obj.has("data")) {
                JSONArray array = obj.optJSONArray("data");
                int k = 0;
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonObject = array.optJSONObject(i);
                    LogUtils.v(TAG, "jsonObject = " + jsonObject);

                    DataBaseManager.RecordsTable item = new DataBaseManager.RecordsTable();
                    item.coverJson(jsonObject.toString());
                    setMessage(item.title, new MyExpandableListAdapter(mContext)
                            .getDuration(item.start_time, item.end_time));
                    showNormal();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
