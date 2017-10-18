package com.yang.backup;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.yang.basic.LogUtils;
import com.yang.basic.LunarCalendar;
import com.yang.basic.MyCalendarHelp;

import java.util.Calendar;
import java.util.List;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

    private final String TAG = MyExpandableListAdapter.class.getSimpleName();
    private Context mContext;
    private MyCalendarHelp m_CalHelp;
    String[] mGroupStrings;
    private List<List<DataBaseManager.RecordsTable>> record;

    public MyExpandableListAdapter(Context c, String[] group,
                                   List<List<DataBaseManager.RecordsTable>> child) {
        mContext = c;
        m_CalHelp = new MyCalendarHelp(mContext);
        mGroupStrings = group;
        record = child;
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
            convertView = View.inflate(mContext, R.layout.item_main_group, null);
        TextView title = (TextView) convertView.findViewById(R.id.textview_item_main_group_title);
        String s = mGroupStrings[groupPosition];
        Calendar cal = m_CalHelp.StringToCalendar(s, m_CalHelp.DATE_FORMAT_SQL);
        String e = m_CalHelp.CalendarToString(cal, m_CalHelp.DATE_FORMAT_DISPLAY);

        title.setText(e.substring(0, 11) + m_CalHelp.getWeekString(cal) + " "
                + mContext.getResources().getString(R.string.lunar) + " "
                + new LunarCalendar().GetLunar(cal));
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

    String getDisplayDate(long lDuration){
        if (lDuration / (24 * 60) != 0) {
            return lDuration / (24 * 60) + mContext.getResources().getString(R.string.day);
        } else if (lDuration / 60 != 0) {
            return lDuration / 60 + mContext.getResources().getString(R.string.hour);
        } else {
            return lDuration % 60 + mContext.getResources().getString(R.string.minute);
        }
    }

    String getDuration(String start, String end) {
        Calendar C_start, C_end, C_now;
        long lDuration, lTemp;
        String temp1, temp2;
        C_start = m_CalHelp.StringToCalendar(start, m_CalHelp.DATE_FORMAT_SQL);
        C_end = m_CalHelp.StringToCalendar(end, m_CalHelp.DATE_FORMAT_SQL);
        C_now = Calendar.getInstance();
        if(C_now.before(C_start)){
            lDuration = (m_CalHelp.getDiff(C_start, C_now)) / 60000;
            temp1 = mContext.getResources().getString(R.string.will_start);
            temp2 = getDisplayDate(lDuration);
            return temp2 + temp1;
        }else if(C_now.after(C_end)){
            lDuration = (m_CalHelp.getDiff(C_end, C_now)) / 60000;
            temp1 = mContext.getResources().getString(R.string.ended);
            temp2 = getDisplayDate(lDuration);
            return temp1 + temp2;
        }else{
            lDuration = (m_CalHelp.getDiff(C_end, C_now)) / 60000;
            temp1 = mContext.getResources().getString(R.string.end);
            temp2 = getDisplayDate(lDuration);
            return temp2 + temp1;
        }
    }
}
