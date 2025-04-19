package com.example.mosque;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME    = "MyAppDB.db";
    private static final int    DB_VERSION = 1;
    private static final String TABLE_NAME = "prayer_times";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // if you need to create the table from scratch:
        db.execSQL(
                "CREATE TABLE " + TABLE_NAME + " (" +
                        "id INTEGER PRIMARY KEY, " +
                        "fajr TEXT, " +
                        "dhuhr TEXT, " +
                        "asr TEXT, " +
                        "magrib TEXT, " +
                        "isha TEXT, " +
                        "updated_at TEXT, " +
                        "synced INTEGER" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /** Fetches the first (and only) row from prayer_times */
    public PrayerTimes getPrayerTimes() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                TABLE_NAME,
                new String[]{ "id","fajr","dhuhr","asr","magrib","isha","updated_at" },
                null, null, null, null, null
        );

        if (c != null && c.moveToFirst()) {
            int idIndex = c.getColumnIndex("id");
            int fajrIndex = c.getColumnIndex("fajr");
            int dhuhrIndex = c.getColumnIndex("dhuhr");
            int asrIndex = c.getColumnIndex("asr");
            int magribIndex = c.getColumnIndex("magrib");
            int ishaIndex = c.getColumnIndex("isha");
            int updatedAtIndex = c.getColumnIndex("updated_at");
            // Check if any of the column indexes are -1, indicating the column name does not exist
            if (idIndex == -1 || fajrIndex == -1 || dhuhrIndex == -1 || asrIndex == -1 ||
                    magribIndex == -1 || ishaIndex == -1 || updatedAtIndex == -1) {
                Log.e("DBHelper", "One or more column names are invalid.");
                c.close();
                return null;  // or throw an exception or handle error as appropriate
            }

            PrayerTimes times = new PrayerTimes(
                    c.getInt(idIndex),
                    c.getString(fajrIndex),
                    c.getString(dhuhrIndex),
                    c.getString(asrIndex),
                    c.getString(magribIndex),
                    c.getString(ishaIndex),
                    c.getString(updatedAtIndex)
            );
            c.close();
            return times;
        } else {
            // If no data exists, insert dummy data
            insertDummyData(db);
            c.close();
            return getPrayerTimes(); // Recursively call to get the newly inserted data
        }
    }
    private void insertDummyData(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("fajr", "05:00");
        values.put("dhuhr", "12:30");
        values.put("asr", "15:45");
        values.put("magrib", "18:20");
        values.put("isha", "19:45");
        values.put("updated_at", "2023-04-19T12:00:00");
        values.put("synced", 0);
        db.insert(TABLE_NAME, null, values);
    }
}
