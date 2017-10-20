package com.yang.backup;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.yang.basic.LogUtils;
import com.yang.basic.MyCalendarHelp;
import com.yang.basic.MySQLHelp;

import static android.database.Cursor.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class DataBaseManager {
    private final String TAG = DataBaseManager.class.getSimpleName();

    private SQLiteDatabase mDatabase;
    private static DataBaseManager instance;
    Context m_context;
    private MyCalendarHelp m_CalHelp;
    protected static final String DATABASE_NAME = "backup.db";
    String Record_table = "records";
    String createTable[] = {"create Table records(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "title TEXT, local TEXT, start_time datetime, end_time datetime, finish boolean,"
            + "finish_time datetime, repeat TEXT,"
            + "remind datetime, description TEXT);"};

    public DataBaseManager(Context context) {
        SQLiteOpenHelper mDatabaseHelper;
        m_context = context;
        m_CalHelp = new MyCalendarHelp(m_context);
        mDatabaseHelper = new MySQLHelp(context, DATABASE_NAME, createTable);
        mDatabase = mDatabaseHelper.getWritableDatabase();
    }

    public static synchronized DataBaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataBaseManager(context);
        }
        return instance;
    }

    public int getRecordCount(){
        int iCount;
        Cursor cursor = mDatabase.rawQuery("select * from " + Record_table, null);
        iCount = cursor.getCount();
        cursor.close();
        return iCount;
    }

    public RecordsTable getRecordByID(int iID){
        RecordsTable table = new RecordsTable();
        Cursor cursor = mDatabase.rawQuery("select * from " + Record_table +
                " where " + RecordsTable.ID + " = " + iID, null);
        if(cursor.getCount() != 1){
            cursor.close();
            return null;
        }
        cursor.moveToFirst();
        table.id = cursor.getInt(0);
        table.title = cursor.getString(1);
        table.local = cursor.getString(2);
        table.start_time = cursor.getString(3);
        table.end_time = cursor.getString(4);
        if(cursor.getInt(5) == 1){
            table.finish = true;
        }else{
            table.finish = false;
        }
        table.finish_time = cursor.getString(6);
        table.repeat = cursor.getString(7);
        table.remind = cursor.getString(8);
        table.description = cursor.getString(9);
        cursor.close();
        return table;
    }

    public RecordsTable getFutureRecord(Calendar cal){
        RecordsTable table = new RecordsTable();
        Cursor cursor = mDatabase.rawQuery("select * from " + Record_table +
                " where " + RecordsTable.START_TIME +
                ">'" + m_CalHelp.CalendarToString(cal, m_CalHelp.DATE_FORMAT_SQL) + "'"+
                " and " + RecordsTable.FINISH + "= 0" +
                " ORDER BY " + RecordsTable.START_TIME, null);
        if(cursor.getCount() == 0){
            cursor.close();
            return null;
        }
        cursor.moveToFirst();
        table.id = cursor.getInt(0);
        table.title = cursor.getString(1);
        table.local = cursor.getString(2);
        table.start_time = cursor.getString(3);
        table.end_time = cursor.getString(4);
        if(cursor.getInt(5) == 1){
            table.finish = true;
        }else{
            table.finish = false;
        }
        table.finish_time = cursor.getString(6);
        table.repeat = cursor.getString(7);
        table.remind = cursor.getString(8);
        table.description = cursor.getString(9);
        cursor.close();
        return table;
    }

    private String getJSONObject(Cursor cursor){
        JSONObject out = new JSONObject();
        try {
            JSONArray resultSet = new JSONArray();
            while (cursor.moveToNext()) {
                JSONObject jsonObject = new JSONObject();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    String colName = cursor.getColumnName(i);
                    int type = cursor.getType(i);
                    String colValue = cursor.getString(i) != null ? cursor.getString(i) : "";
                    switch (type) {
                        case FIELD_TYPE_INTEGER:
                            jsonObject.put(colName, Long.valueOf(colValue));
                            break;
                        case FIELD_TYPE_STRING:
                            jsonObject.put(colName, colValue);
                            break;
                        case FIELD_TYPE_FLOAT:
                            jsonObject.put(colName, Float.valueOf(colValue));
                            break;
                        default:
                            break;
                    }
                }
                resultSet.put(jsonObject);
            }
            out.put("data", resultSet);
            out.put("end", cursor.getCount());
            cursor.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LogUtils.v(TAG, out.toString());
        return out.toString();
    }

    public String getFinishedRecordList(){
        Cursor cursor = mDatabase.rawQuery("select * from " + Record_table +
                " where " + RecordsTable.FINISH + " = 1" +
                " ORDER BY " + RecordsTable.END_TIME + " desc", null);
        return getJSONObject(cursor);
    }

    public String getStartedRecordList(Calendar cal){
        Cursor cursor = mDatabase.rawQuery("select * from " + Record_table +
                " where " + RecordsTable.START_TIME +
                "<='" + m_CalHelp.CalendarToString(cal, m_CalHelp.DATE_FORMAT_SQL) + "'"+
                " and " + RecordsTable.FINISH + "= 0" +
                " ORDER BY " + RecordsTable.END_TIME, null);
        return getJSONObject(cursor);
    }

    public String getFutureRecordList(Calendar cal){
        Cursor cursor = mDatabase.rawQuery("select * from " + Record_table +
                " where " + RecordsTable.START_TIME +
                ">'" + m_CalHelp.CalendarToString(cal, m_CalHelp.DATE_FORMAT_SQL) + "'"+
                " and " + RecordsTable.FINISH + "= 0" +
                " ORDER BY " + RecordsTable.START_TIME, null);
        return getJSONObject(cursor);
    }


    public boolean insertRecord(ContentValues values){
        long count = mDatabase.insertWithOnConflict(Record_table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        MyAlarmManager alarm = new MyAlarmManager(m_context);
        String sTitle = values.getAsString(RecordsTable.TITLE);
        String sStart_time = values.getAsString(RecordsTable.START_TIME);
        String sEnd_time = values.getAsString(RecordsTable.END_TIME);
        alarm.setAlarm((int)count, sTitle, sEnd_time, m_CalHelp.StringToCalendar(sStart_time));
        return true;
    }

    public boolean insertRecord(RecordsTable table){
        ContentValues values = new ContentValues();

        values.put(RecordsTable.TITLE, table.title);
        values.put(RecordsTable.LOCAL, table.local);
        values.put(RecordsTable.START_TIME, table.start_time);
        values.put(RecordsTable.END_TIME, table.end_time);
        values.put(RecordsTable.FINISH, table.finish);
        values.put(RecordsTable.FINISH_TIME, table.finish_time);
        values.put(RecordsTable.REPEAT, table.repeat);
        values.put(RecordsTable.REMIND, table.remind);
        values.put(RecordsTable.DESCRIPTION, table.description);
        long count = mDatabase.insertWithOnConflict(Record_table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        MyAlarmManager alarm = new MyAlarmManager(m_context);
        alarm.setAlarm((int)count, table.title, table.end_time, m_CalHelp.StringToCalendar(table.start_time));
        return true;
    }

    public boolean updateRecord(ContentValues values){
        mDatabase.update(Record_table, values, "_ID=?",
                new String[]{values.getAsString(RecordsTable.ID)});
        MyAlarmManager alarm = new MyAlarmManager(m_context);
        int id = values.getAsInteger(RecordsTable.ID);
        String sTitle = values.getAsString(RecordsTable.TITLE);
        String sStart_time = values.getAsString(RecordsTable.START_TIME);
        String sEnd_time = values.getAsString(RecordsTable.END_TIME);
        alarm.setAlarm(id, sTitle, sEnd_time, m_CalHelp.StringToCalendar(sStart_time));
        return true;
    }

    public boolean updateRecord(RecordsTable table){
        ContentValues values = new ContentValues();
        values.put(RecordsTable.TITLE, table.title);
        values.put(RecordsTable.LOCAL, table.local);
        values.put(RecordsTable.START_TIME, table.start_time);
        values.put(RecordsTable.END_TIME, table.end_time);
        values.put(RecordsTable.FINISH, table.finish);
        values.put(RecordsTable.FINISH_TIME, table.finish_time);
        values.put(RecordsTable.REPEAT, table.repeat);
        values.put(RecordsTable.REMIND, table.remind);
        values.put(RecordsTable.DESCRIPTION, table.description);
        mDatabase.update(Record_table, values, "_ID=?",
                new String[]{Integer.toString(table.id)});
        MyAlarmManager alarm = new MyAlarmManager(m_context);
        alarm.setAlarm(table.id, table.title, table.end_time, m_CalHelp.StringToCalendar(table.start_time));
        return true;
    }

    public boolean DeleteRecordbyID(int iID){
        mDatabase.delete(Record_table, "_ID=?",
                new String[]{Integer.toString(iID)});
        return true;
    }

    public boolean DeleteFinishedRecord(){
        mDatabase.delete(Record_table, "finish=?", new String[]{"1"});
        return true;
    }

    static public class RecordsTable implements Cloneable{
        public static final String ID = "_id";
        public static final String TITLE = "title";
        public static final String LOCAL = "local";
        public static final String START_TIME = "start_time";
        public static final String END_TIME = "end_time";
        public static final String FINISH = "finish";
        public static final String FINISH_TIME = "finish_time";
        public static final String REPEAT = "repeat";
        public static final String REMIND = "remind";
        public static final String DESCRIPTION = "description";

        int id;
        String title;
        String local;
        String start_time;
        String end_time;
        boolean finish;
        String finish_time;
        String repeat;
        String remind;
        String description;

        public void coverJson(String json) {
            if (TextUtils.isEmpty(json))
                return;
            try {
                JSONObject obj = new JSONObject(json);
                id = obj.optInt(ID);
                title = obj.optString(TITLE);
                local = obj.optString(LOCAL);
                start_time = obj.optString(START_TIME);
                end_time = obj.optString(END_TIME);
                finish = obj.optBoolean(FINISH);
                finish_time = obj.optString(FINISH_TIME);
                repeat = obj.optString(REPEAT);
                remind = obj.optString(REMIND);
                description = obj.optString(DESCRIPTION);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public Object clone() {
            RecordsTable table = null;
            try{
                table = (RecordsTable)super.clone();
            }catch(CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return table;
        }
    }
}
