package com.yang.backup;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yang.basic.MyCalendarHelp;
import com.yang.mydialog.MyDateTimeDialog;
import com.yang.mydialog.MyRadioDialog;

import java.util.Calendar;

public class ActivityAdd extends Activity implements View.OnClickListener {

    private static final String TAG = "ActivityAdd";

    private ImageView OK;
    private ImageView cancel;

    private EditText title;
    private EditText location;
    private EditText description;

    private LinearLayout start;
    private LinearLayout end;
    private LinearLayout repeat;
    private LinearLayout remind;

    private TextView text_start;
    private TextView text_end;
    private TextView text_repeat;
    private TextView text_remind;

    public MyCalendarHelp m_CalHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_add);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar_add);

        m_CalHelp = new MyCalendarHelp(this);
        Calendar cal = Calendar.getInstance();

        OK = (ImageView) findViewById(R.id.title_ok);
        cancel = (ImageView) findViewById(R.id.title_cancel);

        title = (EditText) findViewById(R.id.edittext_add_title);
        location = (EditText) findViewById(R.id.edittext_add_location);
        description = (EditText) findViewById(R.id.edittext_add_description);

        start = (LinearLayout) findViewById(R.id.linearlayout_add_start);
        end = (LinearLayout) findViewById(R.id.linearlayout_add_end);
        repeat = (LinearLayout) findViewById(R.id.linearlayout_add_repeat);
        remind = (LinearLayout) findViewById(R.id.linearlayout_add_remind);

        text_start = (TextView) findViewById(R.id.textview_add_start);
        text_end = (TextView) findViewById(R.id.textview_add_end);
        text_repeat = (TextView) findViewById(R.id.textview_add_repeat);
        text_remind = (TextView) findViewById(R.id.textview_add_remind);

        text_start.setText(m_CalHelp.CalendarToString(cal, m_CalHelp.DATE_FORMAT_DISPLAY));
        cal.add(Calendar.HOUR, 1);
        text_end.setText(m_CalHelp.CalendarToString(cal, m_CalHelp.DATE_FORMAT_DISPLAY));

        OK.setOnClickListener(this);
        cancel.setOnClickListener(this);
        repeat.setOnClickListener(this);
        remind.setOnClickListener(this);
        start.setOnClickListener(this);
        end.setOnClickListener(this);
    }

    public void onClick(View v) {
        MyDateTimeDialog m_DateTime_Dialog;
        MyRadioDialog m_Radio_Dialog;
        Calendar calendar;
        switch (v.getId()) {
            case R.id.linearlayout_add_start:
                String sDate = text_start.getText().toString();
                calendar = m_CalHelp.StringToCalendar(sDate, m_CalHelp.DATE_FORMAT_DISPLAY);
                m_DateTime_Dialog = new MyDateTimeDialog(this, new MyDateTimeDialog.ResultHandler() {
                    @Override
                    public void handle(Calendar cal) {
                        text_start.setText(
                                m_CalHelp.CalendarToString(cal, m_CalHelp.DATE_FORMAT_DISPLAY));
                        Calendar end_cal = m_CalHelp.StringToCalendar(
                                text_end.getText().toString(), m_CalHelp.DATE_FORMAT_DISPLAY);
                        cal.add(Calendar.HOUR, 1);
                        if (end_cal.before(cal)) {
                            text_end.setText(
                                    m_CalHelp.CalendarToString(cal, m_CalHelp.DATE_FORMAT_DISPLAY));
                        }
                    }
                }, calendar);
                m_DateTime_Dialog.setIsLoop(true);
                m_DateTime_Dialog.show();
                break;
            case R.id.linearlayout_add_end:
                sDate = text_end.getText().toString();
                calendar = m_CalHelp.StringToCalendar(sDate, m_CalHelp.DATE_FORMAT_DISPLAY);

                m_DateTime_Dialog = new MyDateTimeDialog(this, new MyDateTimeDialog.ResultHandler() {
                    @Override
                    public void handle(Calendar cal) {
                        String temp = m_CalHelp.CalendarToString(cal, m_CalHelp.DATE_FORMAT_DISPLAY);
                        text_end.setText(temp);
                    }
                }, calendar);
                m_DateTime_Dialog.setIsLoop(true);
                m_DateTime_Dialog.show();
                break;

            case R.id.title_ok:
                String sTitle = title.getText().toString();
                String sLocation = location.getText().toString();
                String sDescription = description.getText().toString();
                String sStart_time = m_CalHelp.CalendarToString(
                        m_CalHelp.StringToCalendar(text_start.getText().toString(),
                                m_CalHelp.DATE_FORMAT_DISPLAY),
                        m_CalHelp.DATE_FORMAT_SQL);
                String sEnd_time = m_CalHelp.CalendarToString(
                        m_CalHelp.StringToCalendar(text_end.getText().toString(),
                                m_CalHelp.DATE_FORMAT_DISPLAY),
                        m_CalHelp.DATE_FORMAT_SQL);
                String sRepeat = text_repeat.getText().toString();
                String sRemind = text_remind.getText().toString();


                ContentValues values = new ContentValues();

                values.put(DataBaseManager.RecordsTable.TITLE, sTitle);
                values.put(DataBaseManager.RecordsTable.LOCAL, sLocation);
                values.put(DataBaseManager.RecordsTable.START_TIME, sStart_time);
                values.put(DataBaseManager.RecordsTable.END_TIME, sEnd_time);
                values.put(DataBaseManager.RecordsTable.REPEAT, sRepeat);
                values.put(DataBaseManager.RecordsTable.REMIND, sRemind);
                values.put(DataBaseManager.RecordsTable.DESCRIPTION, sDescription);

                DataBaseManager.getInstance(this).insertRecord(values);
                finish();
                break;
            case R.id.title_cancel:
                Toast.makeText(this, "cancel", Toast.LENGTH_LONG).show();
                finish();
                break;
            case R.id.linearlayout_add_repeat:
                m_Radio_Dialog = new MyRadioDialog(this, new MyRadioDialog.ResultHandler() {
                    @Override
                    public void handle(String str) {
                        text_repeat.setText(str);
                    }
                }, getResources().getStringArray(R.array.repeat_type),
                        text_repeat.getText().toString());
                m_Radio_Dialog.setTitle(getString(R.string.repeat));
                m_Radio_Dialog.show();
                break;

            case R.id.linearlayout_add_remind:
                m_Radio_Dialog = new MyRadioDialog(this, new MyRadioDialog.ResultHandler() {
                    @Override
                    public void handle(String str) {
                        text_remind.setText(str);
                    }
                }, getResources().getStringArray(R.array.remind_type),
                        text_remind.getText().toString());
                m_Radio_Dialog.setTitle(getString(R.string.remind));
                m_Radio_Dialog.show();
                break;
        }
    }
}