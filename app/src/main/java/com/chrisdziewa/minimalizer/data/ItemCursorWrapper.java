package com.chrisdziewa.minimalizer.data;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.chrisdziewa.minimalizer.Item;
import com.chrisdziewa.minimalizer.data.ItemContract.ItemEntry;

/**
 * Created by Chris on 10/25/2017.
 */

public class ItemCursorWrapper extends CursorWrapper {

    public ItemCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Item getItem() {
        int idIndex = getColumnIndex(ItemEntry._ID);
        int nameIndex = getColumnIndex(ItemEntry.COLUMN_NAME);
        int keptIndex = getColumnIndex(ItemEntry.COLUMN_KEEP);

        final long _id = getLong(idIndex);
        String itemName = getString(nameIndex);
        boolean isKept = getInt(keptIndex) == 1;

        return new Item(_id, itemName, isKept);
    }
}
