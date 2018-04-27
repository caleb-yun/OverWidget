package com.cogentworks.overwidget;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class SQLHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "com.cogentworks.overwidget.db";
    private static final String TABLE_NAME = "LIST";
    private static final String COL_NAME = "item";
    private static final int DB_VERSION = 1;

    public SQLHelper(Context context) {
        //1 is to-do list database version
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = String.format("CREATE TABLE %s (ID INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL);", TABLE_NAME, COL_NAME);
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertNewItem(String item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAME, item);

        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void deleteItem(String item) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COL_NAME + " = ?", new String[]{item});
        db.close();
    }

    public ArrayList<String> getList() {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, new String[]{COL_NAME}, null, null, null, null, null, null);
        while (c.moveToNext()) {
            int i = c.getColumnIndex(COL_NAME);
            list.add(c.getString(i));
        }
        c.close();
        db.close();
        return list;
    }
}
