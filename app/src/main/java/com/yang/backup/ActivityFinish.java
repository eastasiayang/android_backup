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
    MyAdapter adapter;

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
                intent.setClass(getApplicationContext(), ActivityAddOrModify.class);
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
        result = DataBaseManager.getInstance(this).getPastedRecordList(Calendar.getInstance());
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
        adapter = new MyAdapter(this);
        expandablelistview.setAdapter(adapter);
        int groupCount = expandablelistview.getCount();
        for (int i = 0; i < groupCount; i++) {
            expandablelistview.expandGroup(i);
        }
        super.onResume();
    }



    public class MyAdapter extends BaseExpandableListAdapter {

        private Context mContext;

        public MyAdapter(Context c) {
            mContext = c;
        }

        @Override
        public int getGroupCount() {
            int i;
            for (i = 0; i < mGroupStrings.length; i++) {
                if (mGroupStrings[i] == null) {
                    break;
                }
            }
            return i;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return record.get(groupPosition).size();
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }


        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = View.inflate(getApplicationContext(), R.layout.item_main_group, null);
            TextView title = (TextView) convertView.findViewById(R.id.textview_item_main_group_title);
            String s = mGroupStrings[groupPosition];
            Calendar cal = m_CalHelp.StringToCalendar(s, m_CalHelp.DATE_FORMAT_SQL);
            String e = m_CalHelp.CalendarToString(cal, m_CalHelp.DATE_FORMAT_DISPLAY);

            title.setText(e.substring(0, 11) + m_CalHelp.getWeekString(cal) + " "
                    + getResources().getString(R.string.lunar) + " "
                    + new LunarCalendar().GetLunar(cal));
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = View.inflate(getApplicationContext(), R.layout.item_main_child, null);
            TextView title = (TextView) convertView.findViewById(
                    R.id.textview_item_main_child_title);
            TextView end = (TextView) convertView.findViewById(
                    R.id.textview_item_main_child_time);

            title.setText(record.get(groupPosition).get(childPosition).title);

            String e = record.get(groupPosition).get(childPosition).end_time;
            String temp = getResources().getString(R.string.ended) + getDuration(e);
            end.setText(temp);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition,
                                         int childPosition) {
            return true;
        }

        String getDuration(String end) {
            Calendar C_end, C_now;
            long lDuration;
            C_end = m_CalHelp.StringToCalendar(end, m_CalHelp.DATE_FORMAT_SQL);
            C_now = Calendar.getInstance();
            lDuration = (m_CalHelp.getDiff(C_end, C_now))/60000;
            LogUtils.d(TAG, "lDuration = " + lDuration);
            if(lDuration/(24*60) != 0){
                return lDuration/(24*60) + getResources().getString(R.string.day);
            }
            if(lDuration/60 != 0){
                return lDuration/60 + getResources().getString(R.string.hour);
            }else{
                return lDuration%60 + getResources().getString(R.string.minute);
            }
        }
    }
}
