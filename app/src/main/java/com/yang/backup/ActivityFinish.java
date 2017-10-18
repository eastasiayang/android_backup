package com.yang.backup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
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

public class ActivityFinish extends Activity {

    private final String TAG = ActivityFinish.class.getSimpleName();
    String[] mGroupStrings;
    private List<List<DataBaseManager.RecordsTable>> record;
    List<DataBaseManager.RecordsTable> item_list;

    ImageView back;
    ExpandableListView expandablelistview;
    private MyCalendarHelp m_CalHelp;
    MyExpandableListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past);

        m_CalHelp = new MyCalendarHelp(this);
        record = new ArrayList<>();

        back = (ImageView) findViewById(R.id.imageview_past_back);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        expandablelistview = (ExpandableListView) findViewById(R.id.expandablelistview_past_record);
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
    }

    @Override
    public void onResume() {
        LogUtils.v(TAG, "onResume");
        record.clear();
        String result;
        result = DataBaseManager.getInstance(this).getFinishedRecordList();
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
}
