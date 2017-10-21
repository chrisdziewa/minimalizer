package com.chrisdziewa.minimalizer.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Chris on 8/31/2017.
 */

public class ItemContract{

    public static final String CONTENT_AUTHORITY = "com.chrisdziewa.minimalizer";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ITEMS = "items";

    public static final class ItemEntry implements BaseColumns {

        // The content uri to work with the items db table
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_ITEMS)
                .build();

        public static final String TABLE_NAME = "items";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_KEEP = "keep";
    }
}
