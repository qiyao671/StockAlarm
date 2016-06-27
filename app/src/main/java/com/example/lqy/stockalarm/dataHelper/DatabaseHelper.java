package com.example.lqy.stockalarm.dataHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by lqy on 16-6-5.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private  static final String DB_NAME = "stockAlarm.db";
    private static final int VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    public DatabaseHelper(Context context, int version) {
        super(context, DB_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create table stock
        String tableStockCreate = "CREATE TABLE IF NOT EXISTS stock(_id INTEGER PRIMARY KEY AUTOINCREMENT, gid varchar(10), code varchar(10), name varchar(30));";
        db.execSQL(tableStockCreate);

        //create table userShare
        String tableUsrShareCreate = "CREATE TABLE IF NOT EXISTS userShare(_id INTEGER PRIMARY KEY AUTOINCREMENT, gid varchar(10), code varchar(10), name varchar(30), max decimal, min decimal, maxPercent decimal, minPercent decimal);";
        db.execSQL(tableUsrShareCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS stock");
        db.execSQL("DROP TABLE IF EXISTS userShare");
        onCreate(db);
    }
}
