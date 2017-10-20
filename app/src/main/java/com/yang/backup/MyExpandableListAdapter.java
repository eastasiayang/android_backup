package com.yang.backup;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
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

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

    private final String TAG = MyExpandableListAdapter.class.getSimpleName();
    private Context mContext;
    private MyCalendarHelp m_CalHelp;
    String[] mGroupStrings;
    private List<List<DataBaseManager.RecordsTable>> record;
    List<DataBaseManager.RecordsTable> item_list;

    public MyExpandableListAdapter(Context c){
        mContext = c;
        m_CalHelp = new MyCalendarHelp(mContext);
    }

    public MyExpandableListAdapter(Context c, String result) {
        mContext = c;
        m_CalHelp = new MyCalendarHelp(mContext);
        record = new ArrayList<>();
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
        return record.get(groupPosition).get(childPosition).id;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = View.inflate(mContext, R.layout.item_main_group, null);
        TextView title = (TextView) convertView.findViewById(R.id.textview_item_main_group_title);
        String s = mGroupStrings[groupPosition];
        Calendar cal = m_CalHelp.StringToCalendar(s);
        String e = m_CalHelp.CalendarToString(cal, m_CalHelp.DATE_FORMAT_DISPLAY);
        String temp = e.substring(0, 11) + m_CalHelp.getWeekString(cal) + " "
                + mContext.getResources().getString(R.string.lunar) + " "
                + new LunarCalendar().GetLunar(cal);
        title.setText(temp);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = View.inflate(mContext, R.layout.item_main_child, null);
        TextView title = (TextView) convertView.findViewById(
                R.id.textview_item_main_child_title);
        TextView end = (TextView) convertView.findViewById(
                R.id.textview_item_main_child_time);

        title.setText(record.get(groupPosition).get(childPosition).title);
        String s = record.get(groupPosition).get(childPosition).start_time;
        String e = record.get(groupPosition).get(childPosition).end_time;
        end.setText(getDuration(s, e));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition,
                                     int childPosition) {
        return true;
    }

    public String getDuration(String start, String end) {
        Calendar C_start, C_end, C_now;
        String temp1, temp2;
        C_start = m_CalHelp.StringToCalendar(start);
        C_end = m_CalHelp.StringToCalendar(end);
        C_now = Calendar.getInstance();
        if(C_now.before(C_start)){
            temp1 = mContext.getResources().getString(R.string.will_start);
            temp2 = m_CalHelp.getDurationTime(C_start, C_now);
            return temp2 + temp1;
        }else if(C_now.after(C_end)){
            temp1 = mContext.getResources().getString(R.string.ended);
            temp2 = m_CalHelp.getDurationTime(C_end, C_now);
            return temp1 + temp2;
        }else{
            temp1 = mContext.getResources().getString(R.string.end);
            temp2 = m_CalHelp.getDurationTime(C_end, C_now);
            return temp2 + temp1;
        }
    }
}
