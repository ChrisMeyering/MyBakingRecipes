package com.baking.chris.mybakingrecipes.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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

public class RecipeListAdapter extends RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder>{
    public static final String TAG = RecipeListAdapter.class.getSimpleName();
    private Context context;
    private List<Recipe> recipes;
    private OnRecipeSelectedListener callback;

    interface OnRecipeSelectedListener {
        void onRecipeSelected(Recipe recipe);
    }


    public RecipeListAdapter(Context context, OnRecipeSelectedListener callback) {
        Log.i(TAG, "RecipeListAdapter");
        this.context = context;
        this.callback = callback;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {

        public RecipeViewHolder(View itemView) {
            super(itemView);
        }

        public void bind(final Recipe recipe){
            ImageView ivRecipeImage = (ImageView) itemView.findViewById(R.id.iv_recipe_image);
            if (recipe.getImage().trim().length() > 0) {
                Picasso.with(context)
                        .load(recipe.getImage())
                        .placeholder(R.drawable.recipe_placeholder)
                        .error(R.drawable.error)
                        .into(ivRecipeImage);
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
            ivRecipeImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.onRecipeSelected(recipe);
                }
            });
            ((TextView) itemView.findViewById(R.id.tv_recipe_name)).setText(recipe.getName());
            ((TextView) itemView.findViewById(R.id.tv_servings)).setText(String.valueOf(recipe.getServings()));
            ((TextView) itemView.findViewById(R.id.tv_ingredients)).setText(String.valueOf(recipe.getNumberOfIngredients()));

        }
    }


    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recipe_list_item, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        holder.bind(recipes.get(position));
    }

    @Override
    public long getItemId(int position) {
        return recipes.get(position).getId();
    }

    @Override
    public int getItemCount() {
        if (recipes == null) return 0;
        return recipes.size();
    }

}
