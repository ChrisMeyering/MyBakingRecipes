package com.baking.chris.mybakingrecipes.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.baking.chris.mybakingrecipes.R;
import com.baking.chris.mybakingrecipes.activity.MainActivity;
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

public class RecipeListFragment extends Fragment implements RecipeListAdapter.OnRecipeSelectedListener {
    public static final String TAG = RecipeListFragment.class.getSimpleName();
    OnRecipeClickListener onRecipeClickListener;
    OnGetRecipesDoneListener onGetRecipesDoneListener;
    RecipeListAdapter adapter;
    RecyclerView rvRecipes;
    Parcelable layoutManagerSavedState = null;


    @Override
    public void onRecipeSelected(Recipe recipe) {
        onRecipeClickListener.onRecipeSelected(recipe);
    }

    public interface OnRecipeClickListener {
        void onRecipeSelected(Recipe recpie);
    }

    public interface OnGetRecipesDoneListener {
        void onDone();
    }

    public RecipeListFragment () { }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(getString(R.string.LAYOUT_MANAGER_KEY),
                rvRecipes.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            layoutManagerSavedState = savedInstanceState.getParcelable(getString(R.string.LAYOUT_MANAGER_KEY));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onRecipeClickListener = (OnRecipeClickListener) context;
            onGetRecipesDoneListener = (OnGetRecipesDoneListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement interfaces");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "OnCreateView");
        getRecipes();
        final View rootView = inflater.inflate(R.layout.fragment_recipe_list, container, false);
        rvRecipes = rootView.findViewById(R.id.rv_recipes);
        rvRecipes.setHasFixedSize(true);
        adapter = new RecipeListAdapter(getActivity(), this);
        rvRecipes.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setAutoMeasureEnabled(true);
        rvRecipes.setLayoutManager(layoutManager);
        return rootView;
    }

    private void getRecipes() {
        ApiInterface mApiInterface = RetrofitClient.getClient().create(ApiInterface.class);

        Call<List<Recipe>> call = mApiInterface.getRecipes();
        call.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                adapter.setRecipes(response.body());
                if (layoutManagerSavedState != null && adapter.getItemCount() > 0) {
                    rvRecipes.getLayoutManager().onRestoreInstanceState(layoutManagerSavedState);
                    layoutManagerSavedState = null;
                }
                Log.i(TAG, "Response obtained");
                onGetRecipesDoneListener.onDone();

            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                Log.i(TAG, "error");
            }
        });
    }

}
