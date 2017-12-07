package com.baking.chris.mybakingrecipes.utils;

import com.baking.chris.mybakingrecipes.data.Recipe;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by chris on 11/16/17.
 */

public interface ApiInterface {
    @GET("baking.json")
    Call<List<Recipe>> getRecipes();
}
