package com.chrisdziewa.minimalizer.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.chrisdziewa.minimalizer.data.ItemContract.ItemEntry;

/**
 * Created by Chris on 8/31/2017.
 */

public class ItemDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "minimalizer.db";
    private static final int DATABASE_VERSION = 1;

    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String DATABASE_CREATION = "CREATE TABLE " + ItemEntry.TABLE_NAME +  " (" +
                ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ItemEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                ItemEntry.COLUMN_KEEP + " INTEGER DEFAULT 0);";

        sqLiteDatabase.execSQL(DATABASE_CREATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
