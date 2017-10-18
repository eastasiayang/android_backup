package com.yang.backup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yang.basic.LogUtils;
import com.yang.basic.LunarCalendar;
import com.yang.basic.MyCalendarHelp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity {
    private final String TAG = MainActivity.class.getSimpleName();
    String[] mGroupStrings;
    private List<List<DataBaseManager.RecordsTable>> record;
    List<DataBaseManager.RecordsTable> item_list;
    private MyHandler m_handler;
    ExpandableListView expandablelistview;
    private MyCalendarHelp m_CalHelp;
    MyExpandableListAdapter adapter;
    LinearLayout lLayout_finish, lLayout_add, lLayout_future;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar_main);

        m_handler = new MyHandler();
        m_handler.sendEmptyMessageDelayed(MyHandler.UPDATE_LIST, 60000);
        m_CalHelp = new MyCalendarHelp(this);

        title = (TextView) findViewById(R.id.titlebar_main_date);
        title.setText(m_CalHelp.CalendarToString(
                Calendar.getInstance(), m_CalHelp.DATE_FORMAT_DISPLAY).substring(0, 8));

        expandablelistview = (ExpandableListView) findViewById(R.id.expandablelistview_main_record);
        expandablelistview.setGroupIndicator(null);
        expandablelistview.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            public boolean onChildClick(ExpandableListView parent, View v,
            int groupPosition, int childPosition, long id) {
                LogUtils.v(TAG, "position = " + childPosition);
                int iId = record.get(groupPosition).get(childPosition).id;
                Intent intent = new Intent();
                intent.putExtra("id", iId);
                intent.setClass(getApplicationContext(), ActivityExamine.class);
                startActivity(intent);
                return true;
            }
        });

        lLayout_finish = (LinearLayout) findViewById(R.id.linearlayout_main_finish);
        lLayout_finish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ActivityFinish.class);
                startActivity(intent);
            }
        });
        lLayout_add = (LinearLayout) findViewById(R.id.linearlayout_main_add);
        lLayout_add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ActivityAddOrModify.class);
                startActivity(intent);
            }
        });
        lLayout_future = (LinearLayout) findViewById(R.id.linearlayout_main_future);
        lLayout_future.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ActivityFuture.class);
                startActivity(intent);
            }
        });
        record = new ArrayList<>();
    }

    @Override
    public void onResume() {
        LogUtils.v(TAG, "onResume");
        record.clear();
        String result;
        result = DataBaseManager.getInstance(this).getStartedRecordList(Calendar.getInstance());
        try {
            JSONObject obj = new JSONObject(result);
            if (obj.has("data")) {
                JSONArray array = obj.optJSONArray("data");
                int k = 0;
                item_list = new ArrayList<>();
                item_list.clear();
                mGroupStrings = new String[array.length()];
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonObject = array.optJSONObject(i);
                    LogUtils.v(TAG, "jsonObject = " + jsonObject);

                    DataBaseManager.RecordsTable item = new DataBaseManager.RecordsTable();
                    item.coverJson(jsonObject.toString());
                    if (i == 0) {
                        mGroupStrings[k++] = item.start_time;
                    } else {
                        if (!item.start_time.substring(0, 10).equals(mGroupStrings[k - 1].substring(0, 10))) {
                            mGroupStrings[k++] = item.start_time;
                            record.add(item_list);
                            item_list = new ArrayList<>();
                            item_list.clear();
                        }
                    }
                    item_list.add(item);
                    if (i == (array.length() - 1)) {
                        record.add(item_list);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter = new MyExpandableListAdapter(this, mGroupStrings, record);
        expandablelistview.setAdapter(adapter);
        int groupCount = expandablelistview.getCount();
        for (int i = 0; i < groupCount; i++) {
            expandablelistview.expandGroup(i);
        }
        super.onResume();
    }

    class MyHandler extends Handler {
        private static final int UPDATE_LIST = 0;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                switch (msg.what) {
                    case UPDATE_LIST:
                        adapter.notifyDataSetChanged();
                        sendEmptyMessageDelayed(UPDATE_LIST, 60000);
                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
