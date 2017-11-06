package com.yang.backup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mob.MobSDK;
import com.yang.basic.LogUtils;
import com.yang.basic.MyCalendarHelp;
import java.util.Calendar;
import com.yang.login.ActivityLogin;


public class MainActivity extends Activity implements View.OnClickListener {
    private final String TAG = MainActivity.class.getSimpleName();

    MyHandler m_handler;
    ExpandableListView expandablelistview;
    MyCalendarHelp m_CalHelp;
    MyExpandableListAdapter adapter;

    LinearLayout LinearLayout_finish, LinearLayout_add, LinearLayout_future, LinearLayout_setting;
    LinearLayout LinearLayout_login, LinearLayout_about;
    TextView title, tips;
    ImageView add;
    DrawerLayout mDrawerLayout;
    ImageView drawer;
    String new_data, old_data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MobSDK.init(this, "21e260d9e834e", "9f4001a9e705f67f657a6ce92d262a79");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();

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
        LinearLayout_setting.setOnClickListener(this);
        LinearLayout_login.setOnClickListener(this);
        LinearLayout_about.setOnClickListener(this);
    }

    void initData() {
        m_handler = new MyHandler(new MyHandler.HandlerCallback() {
            @Override
            public void handle() {
                if (!new_data.equals(old_data) || (adapter.getGroupCount() == 0)) {
                    old_data = new_data;
                    onResume();
                } else {
                    adapter.notifyDataSetChanged();
                    new_data = DataBaseManager.getInstance(MainActivity.this).getFutureRecordList(Calendar.getInstance());
                    m_handler.sendEmptyMessageDelayed(m_handler.UPDATE_MENU, m_handler.UPDATE_DELAY_TIMES);
                }
            }
        });
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
        LinearLayout_setting = (LinearLayout) findViewById(R.id.LinearLayout_drawer_setting);
        LinearLayout_login = (LinearLayout) findViewById(R.id.LinearLayout_drawer_login);
        LinearLayout_about = (LinearLayout) findViewById(R.id.LinearLayout_drawer_about);
    }

    @Override
    public void onResume() {
        LogUtils.v(TAG, "onResume");
        m_handler.sendEmptyMessageDelayed(m_handler.UPDATE_MENU, m_handler.UPDATE_DELAY_TIMES);
        super.onResume();
        DataBaseManager.RecordsTable table;
        new_data = DataBaseManager.getInstance(this).getStartedRecordList(Calendar.getInstance());
        adapter = new MyExpandableListAdapter(this, new_data);
        if (adapter.getGroupCount() == 0) {
            tips.setVisibility(View.VISIBLE);
            expandablelistview.setVisibility(View.GONE);
            table = DataBaseManager.getInstance(this).getFutureRecord(Calendar.getInstance());
            String temp;
            if (table != null) {
                temp = getResources().getString(R.string.now_no_activity) + ", "
                        + getResources().getString(R.string.next_activity) + "\n\n"
                        + table.title + "\n\n" + getResources().getString(R.string.start_time)
                        + m_CalHelp.getDurationTime(Calendar.getInstance(),
                        m_CalHelp.StringToCalendar(table.start_time));
            } else {
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

    @Override
    public void onPause() {
        LogUtils.d(TAG, "onDestroy");
        m_handler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        LogUtils.d(TAG, "onDestroy");
        m_handler.removeCallbacksAndMessages(null);
        super.onDestroy();
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
                break;
            case R.id.LinearLayout_drawer_setting:
                intent = new Intent();
                intent.setClass(MainActivity.this, ActivitySetting.class);
                startActivity(intent);
                break;
            case R.id.LinearLayout_drawer_login:
                intent = new Intent();
                intent.setClass(MainActivity.this, ActivityLogin.class);
                startActivity(intent);
                break;
            case R.id.LinearLayout_drawer_about:
                intent = new Intent();
                intent.setClass(MainActivity.this, ActivityAbout.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
