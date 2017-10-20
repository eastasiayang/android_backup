package com.yang.backup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yang.basic.LogUtils;
import com.yang.basic.MyCalendarHelp;
import java.util.Calendar;


public class MainActivity extends Activity implements View.OnClickListener {
    private final String TAG = MainActivity.class.getSimpleName();
    final int UPDATE_DELAY_TIMES = 6000;

    MyHandler m_handler;
    ExpandableListView expandablelistview;
    MyCalendarHelp m_CalHelp;
    MyExpandableListAdapter adapter;

    LinearLayout LinearLayout_finish, LinearLayout_add, LinearLayout_future;
    TextView title, tips;
    ImageView add;
    DrawerLayout mDrawerLayout;
    ImageView drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();

        m_handler.sendEmptyMessageDelayed(MyHandler.UPDATE_LIST, UPDATE_DELAY_TIMES);
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                LogUtils.i(TAG, "抽屉关闭了...");
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                LogUtils.i(TAG, "抽屉打开了...");
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                LogUtils.i(TAG, "抽屉在滑动...");
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                switch (newState) {
                    case DrawerLayout.STATE_DRAGGING:
                        LogUtils.i(TAG, "拖动状态");
                        break;
                    case DrawerLayout.STATE_IDLE:
                        LogUtils.i(TAG, "精巧状态");
                        break;
                    case DrawerLayout.STATE_SETTLING:
                        LogUtils.i(TAG, "设置状态");
                        break;
                    default:
                        break;
                }
            }
        });

        title.setText(m_CalHelp.CalendarToString(
                Calendar.getInstance(), m_CalHelp.DATE_FORMAT_DISPLAY).substring(0, 8));
        expandablelistview.setGroupIndicator(null);
        expandablelistview.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                long lId = adapter.getChildId(groupPosition, childPosition);
                Intent intent = new Intent();
                intent.putExtra("id", (int) lId);
                intent.setClass(getApplicationContext(), ActivityExamine.class);
                startActivity(intent);
                return true;
            }
        });

        drawer.setOnClickListener(this);
        add.setOnClickListener(this);
        LinearLayout_finish.setOnClickListener(this);
        LinearLayout_add.setOnClickListener(this);
        LinearLayout_future.setOnClickListener(this);
    }

    void initData() {
        m_handler = new MyHandler();
        m_CalHelp = new MyCalendarHelp(this);
    }

    void initView() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer = (ImageView) findViewById(R.id.ImageView_main_drawer);
        title = (TextView) findViewById(R.id.TextView_main_date);
        add = (ImageView) findViewById(R.id.ImageView_main_add);
        tips = (TextView) findViewById(R.id.TextView_main_tips);
        expandablelistview = (ExpandableListView) findViewById(R.id.ExpandableListView_main_record);
        LinearLayout_finish = (LinearLayout) findViewById(R.id.LinearLayout_drawer_finish);
        LinearLayout_add = (LinearLayout) findViewById(R.id.LinearLayout_drawer_add);
        LinearLayout_future = (LinearLayout) findViewById(R.id.LinearLayout_drawer_future);
    }

    @Override
    public void onResume() {
        LogUtils.v(TAG, "onResume");
        super.onResume();
        String result;
        DataBaseManager.RecordsTable table;
        result = DataBaseManager.getInstance(this).getStartedRecordList(Calendar.getInstance());
        adapter = new MyExpandableListAdapter(this, result);
        if (adapter.getGroupCount() == 0) {
            tips.setVisibility(View.VISIBLE);
            expandablelistview.setVisibility(View.GONE);
            table = DataBaseManager.getInstance(this).getFutureRecord(Calendar.getInstance());
            String temp;
            if(table != null){
            temp = getResources().getString(R.string.now_no_activity) + ", "
                    + getResources().getString(R.string.next_activity) + "\n\n"
                    + table.title + "\n\n" + getResources().getString(R.string.start_time)
                    + m_CalHelp.getDurationTime(Calendar.getInstance(),
                    m_CalHelp.StringToCalendar(table.start_time));
            }else{
                temp = getResources().getString(R.string.now_no_activity) + "\n\n"
                + getResources().getString(R.string.press_button_to_add);
            }
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
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ImageView_main_drawer:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.LinearLayout_drawer_finish:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ActivityFinish.class);
                startActivity(intent);
                break;
            case R.id.LinearLayout_drawer_add:
            case R.id.ImageView_main_add:
                intent = new Intent();
                intent.setClass(MainActivity.this, ActivityAddOrModify.class);
                startActivity(intent);
                break;
            case R.id.LinearLayout_drawer_future:
                intent = new Intent();
                intent.setClass(MainActivity.this, ActivityFuture.class);
                startActivity(intent);
            default:
                break;
        }
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
                        sendEmptyMessageDelayed(UPDATE_LIST, UPDATE_DELAY_TIMES);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
