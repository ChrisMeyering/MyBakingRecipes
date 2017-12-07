package com.baking.chris.mybakingrecipes.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.util.Log;

import com.baking.chris.mybakingrecipes.R;
import com.baking.chris.mybakingrecipes.data.Ingredient;
import com.baking.chris.mybakingrecipes.provider.RecipesContract;

/**
 * Created by chris on 12/4/17.
 */

public class RecipeIngredientsService extends IntentService {
    public static final String TAG = RecipeIngredientsService.class.getSimpleName();
    public static final String ACTION_UPDATE_INGREDIENTS_WIDGET
            = "com.baking.chris.mybakingrecipes.widget.action.update_ingredients_widget";

    public RecipeIngredientsService() {
        super("RecipeIngredientsService");
    }
    public static void startActionUpdateIngredients(Context context) {
        Intent intent = new Intent(context, RecipeIngredientsService.class);
        intent.setAction(ACTION_UPDATE_INGREDIENTS_WIDGET);
        context.startService(intent);
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null)
            return;

        String action = intent.getAction();
        if (action.equals(ACTION_UPDATE_INGREDIENTS_WIDGET)) {
            handleActionUpdateIngredientsWidget();
        }
    }

    private void handleActionUpdateIngredientsWidget() {
        Cursor cursor = getContentResolver().query(RecipesContract.RecentEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        Log.i(TAG, "Updating Ingredients Widget");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, MyBakingRecipesWidgetProvider.class));
        Log.i(TAG, "Notifying " + appWidgetIds.length + " widgets");
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_grid_view);
        MyBakingRecipesWidgetProvider.updateIngredientWidgets(this, appWidgetManager, appWidgetIds);
    }
}
