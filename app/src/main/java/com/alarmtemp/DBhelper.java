package com.alarmtemp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBhelper extends SQLiteOpenHelper {

    // Información de la tabla
    public static final String TABLE_TEMPERATURA = "Temperatura";
    public static final String TEMP_ID = "_id";
    public static final String TEMP_MIN = "min";
    public static final String TEMP_MAX = "max";

    // información del a base de datos
    static final String DB_NAME = "DBMIEMBRO";
    static final int DB_VERSION = 1;

    // Información de la base de datos
    private static final String CREATE_TABLE = "create table "
            + TABLE_TEMPERATURA + "(" + TEMP_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TEMP_MIN + " TEXT NOT NULL,"
            + TEMP_MAX + " TEXT NOT NULL);"
            ;

    public DBhelper(Context context) {
        super(context, DB_NAME, null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEMPERATURA);
        onCreate(db);
    }
}
