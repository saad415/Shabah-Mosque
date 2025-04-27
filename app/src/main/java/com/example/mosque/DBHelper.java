package com.example.mosque;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME    = "MyAppDB.db";
    private static final int    DB_VERSION = 2;
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
                        "fajr_iqama TEXT, " +
                        "dhuhr_iqama TEXT, " +
                        "asr_iqama TEXT, " +
                        "magrib_iqama TEXT, " +
                        "isha_iqama TEXT, " +
                        "updated_at TEXT, " +
                        "synced INTEGER" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        if (oldV < 2) {
            // Backup existing data
            Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
            String fajr = "", dhuhr = "", asr = "", magrib = "", isha = "", updatedAt = "";
            
            if (cursor != null && cursor.moveToFirst()) {
                fajr = cursor.getString(cursor.getColumnIndex("fajr"));
                dhuhr = cursor.getString(cursor.getColumnIndex("dhuhr"));
                asr = cursor.getString(cursor.getColumnIndex("asr"));
                magrib = cursor.getString(cursor.getColumnIndex("magrib"));
                isha = cursor.getString(cursor.getColumnIndex("isha"));
                updatedAt = cursor.getString(cursor.getColumnIndex("updated_at"));
                cursor.close();
            }

            // Drop old table
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            
            // Create new table
            onCreate(db);
            
            // Restore data with default iqama times
            ContentValues values = new ContentValues();
            values.put("id", 1);
            values.put("fajr", fajr);
            values.put("dhuhr", dhuhr);
            values.put("asr", asr);
            values.put("magrib", magrib);
            values.put("isha", isha);
            values.put("fajr_iqama", "05:15");
            values.put("dhuhr_iqama", "12:45");
            values.put("asr_iqama", "16:00");
            values.put("magrib_iqama", "18:35");
            values.put("isha_iqama", "20:00");
            values.put("updated_at", updatedAt);
            values.put("synced", 1);
            
            db.insert(TABLE_NAME, null, values);
        }
    }

    /** Fetches the first (and only) row from prayer_times */
    public PrayerTimes getPrayerTimes() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                TABLE_NAME,
                new String[]{ "id","fajr","dhuhr","asr","magrib","isha",
                            "fajr_iqama","dhuhr_iqama","asr_iqama","magrib_iqama","isha_iqama",
                            "updated_at" },
                null, null, null, null, null
        );

        if (c != null && c.moveToFirst()) {
            int idIndex = c.getColumnIndex("id");
            int fajrIndex = c.getColumnIndex("fajr");
            int dhuhrIndex = c.getColumnIndex("dhuhr");
            int asrIndex = c.getColumnIndex("asr");
            int magribIndex = c.getColumnIndex("magrib");
            int ishaIndex = c.getColumnIndex("isha");
            int fajrIqamaIndex = c.getColumnIndex("fajr_iqama");
            int dhuhrIqamaIndex = c.getColumnIndex("dhuhr_iqama");
            int asrIqamaIndex = c.getColumnIndex("asr_iqama");
            int magribIqamaIndex = c.getColumnIndex("magrib_iqama");
            int ishaIqamaIndex = c.getColumnIndex("isha_iqama");
            int updatedAtIndex = c.getColumnIndex("updated_at");

            // Check if any of the column indexes are -1, indicating the column name does not exist
            if (idIndex == -1 || fajrIndex == -1 || dhuhrIndex == -1 || asrIndex == -1 ||
                    magribIndex == -1 || ishaIndex == -1 || fajrIqamaIndex == -1 || dhuhrIqamaIndex == -1 ||
                    asrIqamaIndex == -1 || magribIqamaIndex == -1 || ishaIqamaIndex == -1 || updatedAtIndex == -1) {
                Log.e("DBHelper", "One or more column names are invalid.");
                c.close();
                return null;
            }

            PrayerTimes times = new PrayerTimes(
                    c.getInt(idIndex),
                    c.getString(fajrIndex),
                    c.getString(dhuhrIndex),
                    c.getString(asrIndex),
                    c.getString(magribIndex),
                    c.getString(ishaIndex),
                    c.getString(fajrIqamaIndex),
                    c.getString(dhuhrIqamaIndex),
                    c.getString(asrIqamaIndex),
                    c.getString(magribIqamaIndex),
                    c.getString(ishaIqamaIndex),
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
        values.put("fajr_iqama", "05:15");
        values.put("dhuhr_iqama", "12:45");
        values.put("asr_iqama", "16:00");
        values.put("magrib_iqama", "18:35");
        values.put("isha_iqama", "20:00");
        values.put("updated_at", "2023-04-19T12:00:00");
        values.put("synced", 0);
        db.insert(TABLE_NAME, null, values);
    }

    public void updatePrayerTimes(String fajr, String dhuhr, String asr, String maghrib, String isha, 
                                String fajrIqama, String dhuhrIqama, String asrIqama, String maghribIqama, String ishaIqama,
                                String updatedAt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fajr", fajr);
        values.put("dhuhr", dhuhr);
        values.put("asr", asr);
        values.put("magrib", maghrib);
        values.put("isha", isha);
        values.put("fajr_iqama", fajrIqama);
        values.put("dhuhr_iqama", dhuhrIqama);
        values.put("asr_iqama", asrIqama);
        values.put("magrib_iqama", maghribIqama);
        values.put("isha_iqama", ishaIqama);
        values.put("updated_at", updatedAt);
        values.put("synced", 1);

        // First try to update existing record
        int rows = db.update(TABLE_NAME, values, "id = ?", new String[]{"1"});
        
        // If no rows were updated, insert new record
        if (rows == 0) {
            values.put("id", 1);
            long result = db.insert(TABLE_NAME, null, values);
            if (result == -1) {
                Log.e("DBHelper", "Failed to insert new prayer times");
            } else {
                Log.d("DBHelper", "Successfully inserted new prayer times");
            }
        } else {
            Log.d("DBHelper", "Successfully updated prayer times");
        }
        
        // Verify the update by reading back the data
        PrayerTimes updatedTimes = getPrayerTimes();
        if (updatedTimes != null) {
            Log.d("DBHelper", "Updated times: Fajr=" + updatedTimes.getFajr() + 
                  ", Dhuhr=" + updatedTimes.getDhuhr() + 
                  ", Asr=" + updatedTimes.getAsr() + 
                  ", Maghrib=" + updatedTimes.getMagrib() + 
                  ", Isha=" + updatedTimes.getIsha());
        }
    }
}
