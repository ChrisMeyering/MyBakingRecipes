package com.baking.chris.mybakingrecipes.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.baking.chris.mybakingrecipes.provider.RecipesContract.RecentEntry;
/**
 * Created by chris on 12/4/17.
 */

public class RecipesDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "recipes.db";
    private static final int DATABASE_VERSION = 1;

    public RecipesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_RECENTS_TABLE =
                "CREATE TABLE " + RecentEntry.TABLE_NAME + " ("
                        + RecentEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + RecentEntry.COLUMN_MEASURE + " STRING NOT NULL, "
                        + RecentEntry.COLUMN_QUANTITY + " REAL NOT NULL, "
                        + RecentEntry.COLUMN_INGREDIENT + " STRING NOT NULL);";
        db.execSQL(SQL_CREATE_RECENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RecentEntry.TABLE_NAME);
        onCreate(db);
    }
}
