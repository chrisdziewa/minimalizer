package com.chrisdziewa.minimalizer.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.chrisdziewa.minimalizer.data.ItemContract.ItemEntry;

/**
 * Created by Chris on 9/8/2017.
 */

public class ItemProvider extends ContentProvider {


    // Codes for distinguishing all items from an individual item
    public static final int CODE_ITEMS = 100;
    public static final int CODE_ITEMS_WITH_ID = 101;

    // matcher to sort out the codes above
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private ItemDbHelper mOpenHelper;


    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ItemContract.CONTENT_AUTHORITY;

        /*
         * Add the uri for all items
         * Example content://com.chrisdziewa.minimalizer/items/
         */
        matcher.addURI(authority, ItemContract.PATH_ITEMS, CODE_ITEMS);

        /*
         * Add the uri for an individual item
         * Example content://com.chrisdziewa.minimalizer/items/4
         */
        matcher.addURI(authority, ItemContract.PATH_ITEMS + "/#", CODE_ITEMS_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ItemDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;

        // Thanks to Dan04 and Surya Wijaya Madjid for case insensitive ordering
        // https://stackoverflow.com/questions/2413427/how-to-use-sql-order-by-statement-to-sort-results-case-insensitive
        switch (sUriMatcher.match(uri)) {
            case CODE_ITEMS:
                cursor = mOpenHelper.getReadableDatabase().query(
                        ItemEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        ItemEntry.COLUMN_NAME + " COLLATE NOCASE ASC"
                );
                break;

            case CODE_ITEMS_WITH_ID:
                cursor = mOpenHelper.getReadableDatabase().query(
                        ItemEntry.TABLE_NAME,
                        projection,
                        ItemEntry._ID + " = ?",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            default:
                throw new IllegalArgumentException("Unsupported uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        long newId = 0;

        Uri resultUri;
        switch (sUriMatcher.match(uri)) {
            case CODE_ITEMS:
                newId = mOpenHelper.getWritableDatabase().insert(
                        ItemEntry.TABLE_NAME,
                        null,
                        contentValues
                );

                if (newId > 0) {
                    resultUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, newId);
                } else {
                    throw new SQLException("Failed to insert item");
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        switch (sUriMatcher.match(uri)) {
            case CODE_ITEMS:
                int rowsAffected = mOpenHelper.getWritableDatabase().delete(ItemEntry.TABLE_NAME, null, null);
                return rowsAffected;

            default:
                throw new IllegalArgumentException("Unsupported uri: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] stringArgs) {
        switch (sUriMatcher.match(uri)) {
            case CODE_ITEMS_WITH_ID:
                long id = ContentUris.parseId(uri);

                int rowsAffected = mOpenHelper.getWritableDatabase().update(
                  ItemEntry.TABLE_NAME,
                        contentValues,
                        ItemEntry._ID + " = ?",
                        new String[] { String.valueOf(id)}
                );

                if (rowsAffected > 0) {
                    Uri resultUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);
                    getContext().getContentResolver().notifyChange(resultUri, null);
                } else {
                    throw new SQLException("Failed to update item");
                }

                return rowsAffected;

            default:
                throw new IllegalArgumentException("Unsupported uri: " + uri);
        }
    }
}
