package com.baking.chris.mybakingrecipes.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by chris on 12/4/17.
 */

public class RecipesContract {
    public static final String AUTHORITY = "com.baking.chris.mybakingrecipes";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_RECENT_INGREDIENTS = "recentIngredients";

    public static final class RecentEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_RECENT_INGREDIENTS)
                .build();
        public static final String TABLE_NAME = "recent";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_MEASURE = "measure";
        public static final String COLUMN_INGREDIENT = "ingredient";
        public static Uri buildRecentUriWithID(int id) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        }
    }
}
