package com.baking.chris.mybakingrecipes.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baking.chris.mybakingrecipes.R;
import com.baking.chris.mybakingrecipes.data.Recipe;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by chris on 11/16/17.
 */

public class RecipeListAdapter extends BaseAdapter {
    public static final String TAG = RecipeListAdapter.class.getSimpleName();
    private Context context;
    private List<Recipe> recipes;

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }
    public RecipeListAdapter(Context context) {
        Log.i(TAG, "RecipeListAdapter");
        this.context = context;
    }
    @Override
    public int getCount() {
        if (recipes == null) {
            Log.i(TAG, "recipes are null");
            return 0;
        }
        Log.i(TAG, "num of recipes = " + recipes.size());
        return recipes.size();

    }

    @Override
    public Object getItem(int position) {
        return recipes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return recipes.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Recipe recipe = recipes.get(position);
        Log.i(TAG, "getView");
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.recipe_list_item, parent, false);
        }
        ImageView ivRecipeImage = (ImageView) convertView.findViewById(R.id.iv_recipe_image);
        if (recipe.getImage().trim().length() > 0) {
            Picasso.with(context)
                    .load(recipe.getImage())
                    .placeholder(R.drawable.recipe_placeholder)
                    .error(R.drawable.error)
                    .into(ivRecipeImage/*, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.binding.pbLoadingPoster.setVisibility(View.INVISIBLE);
                    }
                    @Override
                    public void onError() {
                        holder.binding.pbLoadingPoster.setVisibility(View.INVISIBLE);
                    }
                }*/);
        } else {
            int resourceId;
            switch (recipe.getName()) {
                case "Nutella Pie":
                    resourceId = R.drawable.nutella_pie;
                    break;
                case "Yellow Cake":
                    resourceId = R.drawable.yellow_cake;
                    break;
                case "Brownies":
                    resourceId = R.drawable.brownie;
                    break;
                case "Cheesecake":
                    resourceId = R.drawable.cheesecake;
                    break;
                default:
                    resourceId = R.drawable.recipe_placeholder;
                    break;
            }
            ivRecipeImage.setImageResource(resourceId);
        }

        ((TextView) convertView.findViewById(R.id.tv_recipe_name)).setText(recipe.getName());
        ((TextView) convertView.findViewById(R.id.tv_servings)).setText(recipe.getServings().toString());
        ((TextView) convertView.findViewById(R.id.tv_ingredients)).setText(String.valueOf(recipe.getNumberOfIngredients()));

        Log.i(TAG, "recipe " + recipes.get(position).getName());
        return convertView;



    }
}
