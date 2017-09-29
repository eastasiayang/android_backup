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
            + "title TEXT, local TEXT, start_time datetime, end_time datetime, repeat int, "
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

    public String getEndedRecordList(Calendar cal){
        Cursor cursor = mDatabase.rawQuery("select * from " + Record_table +
                " where " + RecordsTable.END_TIME +
                "<'" + m_CalHelp.CalendarToString(cal, m_CalHelp.DATE_FORMAT_SQL) + "'"+
                " ORDER BY " + RecordsTable.END_TIME + " desc", null);
        return getJSONObject(cursor);
    }

    public String getStartedRecordList(Calendar cal){
        Cursor cursor = mDatabase.rawQuery("select * from " + Record_table +
                " where " + RecordsTable.START_TIME +
                "<='" + m_CalHelp.CalendarToString(cal, m_CalHelp.DATE_FORMAT_SQL) + "'"+
                " and " + RecordsTable.END_TIME +
                ">='" + m_CalHelp.CalendarToString(cal, m_CalHelp.DATE_FORMAT_SQL) + "'"+
                " ORDER BY " + RecordsTable.START_TIME + " desc", null);
        return getJSONObject(cursor);
    }

    public String getFutureRecordList(Calendar cal){
        Cursor cursor = mDatabase.rawQuery("select * from " + Record_table +
                " where " + RecordsTable.START_TIME +
                ">'" + m_CalHelp.CalendarToString(cal, m_CalHelp.DATE_FORMAT_SQL) + "'"+
                " ORDER BY " + RecordsTable.START_TIME + " desc", null);
        return getJSONObject(cursor);
    }


    public boolean insertRecord(ContentValues values){
        mDatabase.insertWithOnConflict(Record_table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return true;
    }

    static public class RecordsTable {
        public static final String ID = "_id";
        public static final String TITLE = "title";
        public static final String LOCAL = "local";
        public static final String START_TIME = "start_time";
        public static final String END_TIME = "end_time";
        public static final String REPEAT = "repeat";
        public static final String REMIND = "remind";
        public static final String DESCRIPTION = "description";

        int id;
        String title;
        String local;
        String start_time;
        String end_time;
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
                repeat = obj.optString(REPEAT);
                remind = obj.optString(REMIND);
                description = obj.optString(DESCRIPTION);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
