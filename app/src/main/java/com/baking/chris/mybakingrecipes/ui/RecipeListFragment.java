package com.baking.chris.mybakingrecipes.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.baking.chris.mybakingrecipes.MainActivity;
import com.baking.chris.mybakingrecipes.R;
import com.baking.chris.mybakingrecipes.data.Recipe;
import com.baking.chris.mybakingrecipes.utils.ApiInterface;
import com.baking.chris.mybakingrecipes.utils.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by chris on 11/16/17.
 */

public class RecipeListFragment extends Fragment {
    public static final String TAG = RecipeListFragment.class.getSimpleName();
    OnRecipeClickListener mCallback;
    RecipeListAdapter adapter;
    public interface OnRecipeClickListener {
        void onRecipeSelected(Recipe recpie);
    }
    public RecipeListFragment () { }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnRecipeClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnRecipeClickListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "OnCreateView");
        getRecipes();
        final View rootView = inflater.inflate(R.layout.fragment_recipe_list, container, false);
        GridView gridVew = rootView.findViewById(R.id.recipe_grid_view);
        adapter = new RecipeListAdapter(getContext());
        gridVew.setAdapter(adapter);
        gridVew.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallback.onRecipeSelected((Recipe) adapter.getItem(position));
            }
        });
        return rootView;
    }

    private void getRecipes() {
        ApiInterface mApiInterface = RetrofitClient.getClient().create(ApiInterface.class);

        Call<List<Recipe>> call = mApiInterface.getRecipes();
        call.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                adapter.setRecipes(response.body());

                Log.i(TAG, "Num of recipes: " +response.body().size() );
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                Log.i(TAG, "error");
            }
        });
    }

}
