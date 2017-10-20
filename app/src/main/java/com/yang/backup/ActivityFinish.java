package com.yang.backup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.yang.basic.LogUtils;
import com.yang.basic.MyCalendarHelp;
import com.yang.mydialog.MyAlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ActivityFinish extends Activity {

    private final String TAG = ActivityFinish.class.getSimpleName();

    ImageView back;
    ImageView delete;
    TextView tips;
    ExpandableListView expandablelistview;
    MyCalendarHelp m_CalHelp;
    MyExpandableListAdapter adapter;
    MyAlertDialog m_Alert_Dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);

        m_CalHelp = new MyCalendarHelp(this);

        back = (ImageView) findViewById(R.id.ImageView_finish_back);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        delete = (ImageView) findViewById(R.id.ImageView_finish_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                m_Alert_Dialog = new MyAlertDialog(ActivityFinish.this,
                        new MyAlertDialog.ResultHandler() {
                            @Override
                            public void handle(boolean bOK) {
                                if (bOK) {
                                    DataBaseManager.getInstance(ActivityFinish.this).DeleteFinishedRecord();
                                    onResume();
                                }
                            }
                        });
                m_Alert_Dialog.setTitle(getString(R.string.are_you_sure_delete_all_finish));
                m_Alert_Dialog.show();
            }
        });

        tips = (TextView) findViewById(R.id.TextView_finish_tips);

        expandablelistview = (ExpandableListView) findViewById(R.id.ExpandableListView_finish_record);
        expandablelistview.setGroupIndicator(null);

        expandablelistview.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                LogUtils.v(TAG, "position = " + childPosition);
                long lId = adapter.getChildId(groupPosition, childPosition);
                Intent intent = new Intent();
                intent.putExtra("id", (int)lId);
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
        result = DataBaseManager.getInstance(this).getFinishedRecordList();
        adapter = new MyExpandableListAdapter(this, result);
        if (adapter.getGroupCount() == 0) {
            delete.setImageResource(R.drawable.ic_delete_disable);
            delete.setEnabled(false);
            tips.setVisibility(View.VISIBLE);
            expandablelistview.setVisibility(View.GONE);
            String temp;
            temp = getResources().getString(R.string.now_no_finish_activity) + "\n\n"
                    + getResources().getString(R.string.choose_an_activity_to_finish);
            tips.setText(temp);
        }else{
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
