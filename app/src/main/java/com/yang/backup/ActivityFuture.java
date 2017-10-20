package com.yang.backup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
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

public class ActivityFuture extends Activity {

    private final String TAG = ActivityFuture.class.getSimpleName();

    ImageView back;
    TextView tips;
    ExpandableListView expandablelistview;
    MyCalendarHelp m_CalHelp;
    MyExpandableListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_future);

        m_CalHelp = new MyCalendarHelp(this);

        back = (ImageView) findViewById(R.id.imageview_future_back);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        tips = (TextView) findViewById(R.id.TextView_future_tips);
        expandablelistview = (ExpandableListView) findViewById(R.id.expandablelistview_future_record);
        expandablelistview.setGroupIndicator(null);

        expandablelistview.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                LogUtils.v(TAG, "position = " + childPosition);
                long lId = adapter.getChildId(groupPosition, childPosition);
                Intent intent = new Intent();
                intent.putExtra("id", (int) lId);
                intent.setClass(getApplicationContext(), ActivityExamine.class);
                startActivity(intent);
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        LogUtils.v(TAG, "onResume");
        String result;
        result = DataBaseManager.getInstance(this).getFutureRecordList(Calendar.getInstance());
        adapter = new MyExpandableListAdapter(this, result);
        if (adapter.getGroupCount() == 0) {
            tips.setVisibility(View.VISIBLE);
            expandablelistview.setVisibility(View.GONE);
            String temp;
            temp = getResources().getString(R.string.now_no_future_activity) + "\n\n"
                    + getResources().getString(R.string.back_to_main_activity_to_add);
            tips.setText(temp);
        } else {
            expandablelistview.setVisibility(View.VISIBLE);
            tips.setVisibility(View.GONE);
            expandablelistview.setAdapter(adapter);
            int groupCount = expandablelistview.getCount();
            for (int i = 0; i < groupCount; i++) {
                expandablelistview.expandGroup(i);
            }
        }
        super.onResume();
    }
}
