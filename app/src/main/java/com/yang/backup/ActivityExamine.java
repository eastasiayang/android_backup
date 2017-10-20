package com.yang.backup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yang.basic.LogUtils;
import com.yang.basic.LunarCalendar;
import com.yang.basic.MyCalendarHelp;
import com.yang.mydialog.MyAlertDialog;

import java.util.Calendar;

public class ActivityExamine extends Activity {

    private static final String TAG = "ActivityExamine";

    TextView title;
    TextView location;
    TextView date;
    TextView repeat;
    TextView remind;
    TextView description;
    TextView finish_date;

    LinearLayout LinearLayout_location, LinearLayout_remind, LinearLayout_description,
            LinearLayout_finish_date;

    private int id;
    LinearLayout lLayout_end, lLayout_delete, lLayout_modify;
    MyAlertDialog m_Alert_Dialog;
    DataBaseManager.RecordsTable table;
    public MyCalendarHelp m_CalHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_examine);

        m_CalHelp = new MyCalendarHelp(this);

        id = getIntent().getIntExtra("id", -1);
        table = DataBaseManager.getInstance(this).getRecordByID(id);
        LogUtils.v(TAG, "id = " + id);

        title = (TextView) findViewById(R.id.TextView_examine_title);
        title.setText(table.title);

        location = (TextView) findViewById(R.id.TextView_examine_location);
        LinearLayout_location = (LinearLayout) findViewById(R.id.LinearLayout_examine_location);
        if(table.local.equals("")){
            LinearLayout_location.setVisibility(View.GONE);
        }else{
            location.setText(table.local);
        }

        date = (TextView) findViewById(R.id.TextView_examine_date);
        String temp = m_CalHelp.CalendarToString(
                m_CalHelp.StringToCalendar(table.start_time, m_CalHelp.DATE_FORMAT_SQL),
                m_CalHelp.DATE_FORMAT_DISPLAY) + " - "
                + m_CalHelp.CalendarToString(
                m_CalHelp.StringToCalendar(table.end_time, m_CalHelp.DATE_FORMAT_SQL),
                m_CalHelp.DATE_FORMAT_DISPLAY);
        date.setText(temp);

        repeat = (TextView) findViewById(R.id.TextView_examine_repeat);
        repeat.setText(table.repeat);

        remind = (TextView) findViewById(R.id.TextView_examine_remind);
        LinearLayout_remind = (LinearLayout) findViewById(R.id.LinearLayout_examine_remind);
        if(table.remind.equals(getResources().getString(R.string.none))){
            LinearLayout_remind.setVisibility(View.GONE);
        }else{
            remind.setText(table.remind);
        }

        description = (TextView) findViewById(R.id.TextView_examine_description);
        LinearLayout_description = (LinearLayout) findViewById(R.id.LinearLayout_examine_description);
        if(table.description.equals("")){
            LinearLayout_description.setVisibility(View.GONE);
        }else{
            description.setText(table.description);
        }

        lLayout_end = (LinearLayout) findViewById(R.id.LinearLayout_examine_end);
        lLayout_end.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                m_Alert_Dialog = new MyAlertDialog(ActivityExamine.this,
                        new MyAlertDialog.ResultHandler() {
                            @Override
                            public void handle(boolean bOK) {
                                if (bOK) {
                                    FinishTable();
                                    DataBaseManager.getInstance(ActivityExamine.this).updateRecord(table);
                                    finish();
                                }
                            }
                        });
                m_Alert_Dialog.setTitle(getString(R.string.are_you_sure_finish));
                m_Alert_Dialog.show();
            }
        });
        lLayout_delete = (LinearLayout) findViewById(R.id.LinearLayout_examine_delete);
        lLayout_delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                m_Alert_Dialog = new MyAlertDialog(ActivityExamine.this,
                        new MyAlertDialog.ResultHandler() {
                            @Override
                            public void handle(boolean bOK) {
                                if (bOK) {
                                    DataBaseManager.getInstance(ActivityExamine.this).DeleteRecordbyID(id);
                                    finish();
                                }
                            }
                        });
                m_Alert_Dialog.setTitle(getString(R.string.are_you_sure_delete));
                m_Alert_Dialog.show();
            }
        });
        lLayout_modify = (LinearLayout) findViewById(R.id.LinearLayout_examine_modify);
        lLayout_modify.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("id", id);
                intent.setClass(ActivityExamine.this, ActivityAddOrModify.class);
                startActivity(intent);
                finish();
            }
        });
        finish_date = (TextView) findViewById(R.id.TextView_examine_finish_date);
        LinearLayout_finish_date = (LinearLayout) findViewById(R.id.LinearLayout_examine_finish_date);
        if(table.finish_time.equals("")){
            LinearLayout_finish_date.setVisibility(View.GONE);
        }else{
            temp = getResources().getString(R.string.finish_time) + ": " + m_CalHelp.CalendarToString(
                    m_CalHelp.StringToCalendar(table.finish_time, m_CalHelp.DATE_FORMAT_SQL),
                    m_CalHelp.DATE_FORMAT_DISPLAY);
            finish_date.setText(temp);
            lLayout_end.setVisibility(View.GONE);
            lLayout_modify.setVisibility(View.GONE);
        }
    }

    void FinishTable() {

        Calendar cal = Calendar.getInstance();
        DataBaseManager.RecordsTable new_table = (DataBaseManager.RecordsTable) table.clone();

        Calendar start, end;
        start = m_CalHelp.StringToCalendar(table.start_time, m_CalHelp.DATE_FORMAT_SQL);
        end = m_CalHelp.StringToCalendar(table.end_time, m_CalHelp.DATE_FORMAT_SQL);

        table.finish = true;
        table.finish_time = m_CalHelp.CalendarToString(cal, m_CalHelp.DATE_FORMAT_SQL);
        if (table.repeat.equals(getResources().getString(R.string.every_day))) {
            start.add(Calendar.DAY_OF_YEAR, 1);
            new_table.start_time = m_CalHelp.CalendarToString(start, m_CalHelp.DATE_FORMAT_SQL);
            end.add(Calendar.DAY_OF_YEAR, 1);
            new_table.end_time = m_CalHelp.CalendarToString(end, m_CalHelp.DATE_FORMAT_SQL);
            DataBaseManager.getInstance(ActivityExamine.this).insertRecord(new_table);
        } else if (table.repeat.equals(getResources().getString(R.string.every_week))) {
            start.add(Calendar.DAY_OF_YEAR, 7);
            new_table.start_time = m_CalHelp.CalendarToString(start, m_CalHelp.DATE_FORMAT_SQL);
            end.add(Calendar.DAY_OF_YEAR, 7);
            new_table.end_time = m_CalHelp.CalendarToString(end, m_CalHelp.DATE_FORMAT_SQL);
            DataBaseManager.getInstance(ActivityExamine.this).insertRecord(new_table);
        } else if (table.repeat.equals(getResources().getString(R.string.every_month))) {
            start.add(Calendar.MONTH, 1);
            new_table.start_time = m_CalHelp.CalendarToString(start, m_CalHelp.DATE_FORMAT_SQL);
            end.add(Calendar.MONTH, 1);
            new_table.end_time = m_CalHelp.CalendarToString(end, m_CalHelp.DATE_FORMAT_SQL);
            DataBaseManager.getInstance(ActivityExamine.this).insertRecord(new_table);
        } else if (table.repeat.equals(getResources().getString(R.string.every_year))) {
            start.add(Calendar.YEAR, 1);
            new_table.start_time = m_CalHelp.CalendarToString(start, m_CalHelp.DATE_FORMAT_SQL);
            end.add(Calendar.YEAR, 1);
            new_table.end_time = m_CalHelp.CalendarToString(end, m_CalHelp.DATE_FORMAT_SQL);
            DataBaseManager.getInstance(ActivityExamine.this).insertRecord(new_table);
        } else if (table.repeat.equals(getResources().getString(R.string.every_year_lunar))) {
            start = new LunarCalendar().GetNextLunarYear(start);
            new_table.start_time = m_CalHelp.CalendarToString(start, m_CalHelp.DATE_FORMAT_SQL);
            end = new LunarCalendar().GetNextLunarYear(end);
            new_table.end_time = m_CalHelp.CalendarToString(end, m_CalHelp.DATE_FORMAT_SQL);
            DataBaseManager.getInstance(ActivityExamine.this).insertRecord(new_table);
        } else if (table.repeat.equals(getResources().getString(R.string.user_define))) {
            //wait
        }
    }
}
