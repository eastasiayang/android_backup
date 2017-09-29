package com.yang.basic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLHelp extends SQLiteOpenHelper{
    private final String TAG = MySQLHelp.class.getSimpleName();

    String[] CreateTableSQL;

    public MySQLHelp(Context context, String database_name, String[] CreateTableSQL) {
        super(context, database_name, null, 12);
        this.CreateTableSQL = CreateTableSQL;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        int i=0;
        for(i=0; i<CreateTableSQL.length; i++){
            db.execSQL(CreateTableSQL[i]);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub

    }
}
