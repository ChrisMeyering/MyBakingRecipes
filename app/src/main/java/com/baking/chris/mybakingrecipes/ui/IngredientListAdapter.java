package com.baking.chris.mybakingrecipes.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baking.chris.mybakingrecipes.R;
import com.baking.chris.mybakingrecipes.data.Ingredient;

import java.util.List;

/**
 * Created by chris on 11/19/17.
 */

public class IngredientListAdapter extends RecyclerView.Adapter<IngredientListAdapter.IngredientViewHolder>{
    public static final String TAG = IngredientListAdapter.class.getSimpleName();
    private Context context;
    private List<Ingredient> ingredients;
    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
        notifyDataSetChanged();
    }

    public IngredientListAdapter(Context context, List<Ingredient> ingredients){
        this.context = context;
        this.ingredients = ingredients;
        Log.i(TAG, String.valueOf(ingredients.size()));
    }

    @Override
    public IngredientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.ingredient_list_item, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(IngredientViewHolder holder, int position) {
        holder.bind(ingredients.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (ingredients == null)
            return 0;
        return ingredients.size();
    }

    class IngredientViewHolder extends RecyclerView.ViewHolder {

        public IngredientViewHolder(View itemView) {
            super(itemView);
        }

        public void bind(Ingredient ingredient) {
            ((TextView) itemView.findViewById(R.id.tv_ingredient_name)).setText(ingredient.getIngredient());
            ((TextView) itemView.findViewById(R.id.tv_measure_quantity)).setText(String.valueOf(ingredient.getQuantity()));
            ((TextView) itemView.findViewById(R.id.tv_measure_unit)).setText(ingredient.getMeasure());

        }
    }
}
