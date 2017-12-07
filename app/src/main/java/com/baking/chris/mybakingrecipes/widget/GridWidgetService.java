package com.baking.chris.mybakingrecipes.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.baking.chris.mybakingrecipes.R;
import com.baking.chris.mybakingrecipes.data.Ingredient;
import com.baking.chris.mybakingrecipes.provider.RecipesContract;
import com.baking.chris.mybakingrecipes.provider.RecipesProvider;

/**
 * Created by chris on 12/4/17.
 */

public class GridWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridRemoteViewsFactory(this.getApplicationContext());
    }
}

class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    public static final String TAG = GridRemoteViewsFactory.class.getSimpleName();

    Context context;
    Cursor cursor;

    public GridRemoteViewsFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
    }

    @Override
    public void onDataSetChanged() {

        if (cursor != null) {
            cursor.close();
        }
        Log.i(TAG, "Widget DataSet Changed");
        cursor = context.getContentResolver().query(
                RecipesContract.RecentEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        Log.i(TAG, cursor.getCount() + " ingredients found");
    }

    @Override
    public void onDestroy() {
        cursor.close();
    }

    @Override
    public int getCount() {
        Log.i(TAG, "getCount");
        if (cursor == null)
            return 0;
        return cursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        Log.i(TAG, "getViewAt" + i);
        if (cursor == null || cursor.getCount() == 0) return null;
        cursor.moveToPosition(i);
        Log.i(TAG, "populating view " + i);
        int ingredientNameIdx = cursor.getColumnIndex(RecipesContract.RecentEntry.COLUMN_INGREDIENT);
        int ingredientMeasureIdx = cursor.getColumnIndex(RecipesContract.RecentEntry.COLUMN_MEASURE);
        int ingredientQuantityIdx = cursor.getColumnIndex(RecipesContract.RecentEntry.COLUMN_QUANTITY);
        Ingredient ingredient = new Ingredient();
        ingredient.setIngredient(cursor.getString(ingredientNameIdx));
        ingredient.setMeasure(cursor.getString(ingredientMeasureIdx));
        ingredient.setQuantity(cursor.getFloat(ingredientQuantityIdx));
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_ingredient_list_item);
        views.setTextViewText(R.id.tv_ingredient_name, ingredient.getIngredient());
        views.setTextViewText(R.id.tv_measure_quantity, String.valueOf(ingredient.getQuantity()));
        views.setTextViewText(R.id.tv_measure_unit, ingredient.getMeasure());
        Log.i(TAG, "ingredient = " + ingredient.getIngredient());
        Log.i(TAG, "quantity = " + ingredient.getQuantity());
        Log.i(TAG, "measure = " + ingredient.getMeasure());
        Log.i(TAG, "view " + i + " returned");
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        Log.i(TAG, "getLoadingView");
        return null;
    }


    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
