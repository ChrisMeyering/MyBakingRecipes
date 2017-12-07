package com.baking.chris.mybakingrecipes.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.baking.chris.mybakingrecipes.data.Recipe;

/**
 * Created by chris on 12/4/17.
 */

public class RecipesProvider extends ContentProvider {
    public static final String TAG = RecipesProvider.class.getSimpleName();
    public static final int CODE_RECENT = 100;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private RecipesDbHelper mOpenHelper;


    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(RecipesContract.AUTHORITY, RecipesContract.PATH_RECENT_INGREDIENTS, CODE_RECENT);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new RecipesDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case CODE_RECENT:
                Log.i(TAG, "CODE RECENT matched for query");
                cursor = mOpenHelper.getReadableDatabase().query(
                        RecipesContract.RecentEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case CODE_RECENT:
                db.beginTransaction();
                int rowsInserted = 0 ;
                try {
                    for (ContentValues value : values)
                        if (db.insert(RecipesContract.RecentEntry.TABLE_NAME, null, value) != -1)
                            rowsInserted++;
                } finally {
                    db.endTransaction();
                }
                if(rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsInserted;
            default:
                return super.bulkInsert(uri, values);

        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        long id = -1;
        try {
            switch (sUriMatcher.match(uri)) {
                case CODE_RECENT:
                    id = db.insert(RecipesContract.RecentEntry.TABLE_NAME, null, contentValues);
                    db.setTransactionSuccessful();
                    break;
            }
        } finally {
            db.endTransaction();
        } if (id != -1) {
            getContext().getContentResolver().notifyChange(uri, null);
            return uri.buildUpon().appendPath(String.valueOf(id)).build();
        } else {
            return  null;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int numRowsDeleted = 0;
        if (null == selection) selection = "1";
        switch (sUriMatcher.match(uri)){
            case CODE_RECENT:
                numRowsDeleted = mOpenHelper.getWritableDatabase()
                        .delete(RecipesContract.RecentEntry.TABLE_NAME,
                                selection,
                                selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
