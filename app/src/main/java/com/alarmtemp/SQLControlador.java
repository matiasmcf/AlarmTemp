package com.alarmtemp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SQLControlador {

    private DBhelper dbhelper;
    private Context ourcontext;
    private SQLiteDatabase database;

    public SQLControlador(Context c) {
        ourcontext = c;
    }

    public SQLControlador abrirBaseDeDatos() throws SQLException {
        dbhelper = new DBhelper(ourcontext);
        database = dbhelper.getWritableDatabase();
        return this;
    }

    public void cerrar() {
        dbhelper.close();
    }

    public void insertarDatos(String min,String max) {
        ContentValues cv = new ContentValues();
        cv.put(DBhelper.TEMP_MIN, min);
        cv.put(DBhelper.TEMP_MAX,max);
        database.insert(DBhelper.TABLE_TEMPERATURA, null, cv);
    }

    public Cursor leerDatos() {
        String[] todasLasColumnas = new String[] {
                DBhelper.TEMP_ID,
                DBhelper.TEMP_MAX,
                DBhelper.TEMP_MIN
        };
        Cursor c = database.query(DBhelper.TABLE_TEMPERATURA, todasLasColumnas, null,
                null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }



    public void deleteData(long memberID) {
        database.delete(DBhelper.TABLE_TEMPERATURA, DBhelper.TEMP_ID + "="
                + memberID, null);
    }
}
