package com.cogentworks.overwidget;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;

import java.util.ArrayList;

public class SQLHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "com.cogentworks.overwidget.db";
    private static final String TABLE_NAME = "LIST";
    private static final String COL_NAME = "GsonData";
    private static final int DB_VERSION = 5;

    public SQLHelper(Context context) {
        //1 is to-do list database version
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_NAME + " TEXT NOT NULL, BattleTag text);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void setList(ArrayList<Profile> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

        for (Profile profile : list) {
            ContentValues values = new ContentValues();
            values.put("BattleTag", profile.BattleTag);
            values.put(COL_NAME, profile.toGson());

            db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }

        db.close();
    }

    public void insertNewProfile(String battleTag, Profile profile) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("BattleTag", battleTag);
        values.put(COL_NAME, profile.toGson());

        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void updateItem(String battleTag, Profile profile) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("BattleTag", battleTag);
        values.put(COL_NAME, profile.toGson());

        db.update(TABLE_NAME, values, "BattleTag = ?", new String[]{battleTag});
        db.close();
    }

    public void deleteItem(String item) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "BattleTag = ?", new String[]{item});
        db.close();
    }

    public ArrayList<String> getList(String column) {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, new String[]{column}, null, null, null, null, null, null);
        while (c.moveToNext()) {
            int i = c.getColumnIndex(column);
            list.add(c.getString(i));
        }
        c.close();
        db.close();
        return list;
    }

    public ArrayList<Profile> getList() {
        Gson gson = new Gson();
        ArrayList<String> list = getList(COL_NAME);
        ArrayList<Profile> profiles = new ArrayList<>();
        for (String json : list)
            profiles.add(gson.fromJson(json, Profile.class));
        return profiles;
    }
}
