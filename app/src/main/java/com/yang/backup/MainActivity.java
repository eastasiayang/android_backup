package com.yang.backup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
    MyAdapter adapter;
    WindowManager wm;
    WindowManager.LayoutParams params;
    LinearLayout lLayout_add, lLayout_all;
    View myLayout;
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
        adapter = new MyAdapter(this);
        expandablelistview.setAdapter(adapter);
        int groupCount = expandablelistview.getCount();
        for (int i = 0; i < groupCount; i++) {
            expandablelistview.expandGroup(i);
        }
        createFloatView();
        super.onResume();
    }

    @Override
    public void onPause() {
        LogUtils.v(TAG, "onPause");
        wm.removeView(myLayout);
        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, ActivityAll.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createFloatView() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(getApplicationContext())) {
                //启动Activity让用户授权
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(intent);
                return;
            }
        }
        //加载xml布局
        myLayout = View.inflate(this, R.layout.float_view_main_activity, null);
        lLayout_add = (LinearLayout) myLayout.findViewById(R.id.linearlayout_float_main_add);
        lLayout_all = (LinearLayout) myLayout.findViewById(R.id.linearlayout_float_main_all);
        lLayout_add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ActivityAdd.class);
                startActivity(intent);
            }
        });
        lLayout_all.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ActivityAll.class);
                startActivity(intent);
            }
        });
        wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.format = PixelFormat.RGBX_8888; // 设置图片格式，效果为背景透明(RGBA_8888)
        params.alpha = 0.99f;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 设置悬浮窗的长宽
        params.width = Toolbar.LayoutParams.WRAP_CONTENT;
        params.height = Toolbar.LayoutParams.WRAP_CONTENT;
        //设置悬浮窗的位置
        params.x = 30;
        params.y = 700;
        wm.addView(myLayout, params);
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
            title.setText(record.get(groupPosition).get(childPosition).title);

            String s = record.get(groupPosition).get(childPosition).start_time;
            TextView start = (TextView) convertView.findViewById(
                    R.id.textview_item_main_child_start_time);
            start.setText(s.substring(s.length() - 5, s.length()));

            String e = record.get(groupPosition).get(childPosition).end_time;
            TextView end = (TextView) convertView.findViewById(
                    R.id.textview_item_main_child_end_time);
            end.setText(getDuration(e));
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition,
                                         int childPosition) {
            return true;
        }

        String getDuration(String end) {
            Calendar C_end, C_now;
            int iDuration;
            C_end = m_CalHelp.StringToCalendar(end, m_CalHelp.DATE_FORMAT_SQL);
            C_now = Calendar.getInstance();
            iDuration = (int) (m_CalHelp.getDiff(C_end, C_now))/60000;
            if(iDuration/(24*60) != 0){
                return iDuration/(24*60) + getResources().getString(R.string.day)
                        + getResources().getString(R.string.end);
            }
            if(iDuration/60 != 0){
                return iDuration/60 + getResources().getString(R.string.hour)
                        + getResources().getString(R.string.end);
            }else{
                return iDuration%60 + getResources().getString(R.string.minute)
                        + getResources().getString(R.string.end);
            }

        }
    }
}
