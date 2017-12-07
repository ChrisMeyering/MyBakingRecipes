package com.baking.chris.mybakingrecipes.data;



import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.baking.chris.mybakingrecipes.provider.RecipesContract;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * Created by http://www.jsonschema2pojo.org/ on 11/16/17.
 */

public class Recipe implements Parcelable {
    public static final String TAG = Recipe.class.getSimpleName();

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("ingredients")
    @Expose
    private List<Ingredient> ingredients = new ArrayList<>();
    @SerializedName("steps")
    @Expose
    private List<Step> steps = new ArrayList<>();
    @SerializedName("servings")
    @Expose
    private Integer servings;
    @SerializedName("image")
    @Expose
    private String image;
    public final static Parcelable.Creator<Recipe> CREATOR = new Creator<Recipe>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        public Recipe[] newArray(int size) {
            return (new Recipe[size]);
        }

    }
            ;

    protected Recipe(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.name = (String) in.readValue(String.class.getClassLoader());
        in.readList(this.ingredients, Ingredient.class.getClassLoader());
        in.readList(this.steps, Step.class.getClassLoader());
        this.servings = (Integer) in.readValue(Integer.class.getClassLoader());
        this.image = (String) in.readValue(String.class.getClassLoader());
    }

    public Recipe() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public ContentValues[] getRecipeIngredientsContentValues() {
        ContentValues[] ingredientsCV = new ContentValues[this.ingredients.size()];
        for (int i = 0; i < ingredientsCV.length; i++) {
            Ingredient ingredientPOJO = ingredients.get(i);
            ContentValues ingrdientCV = new ContentValues();
            ingrdientCV.put(RecipesContract.RecentEntry.COLUMN_INGREDIENT, ingredientPOJO.getIngredient());
            ingrdientCV.put(RecipesContract.RecentEntry.COLUMN_MEASURE, ingredientPOJO.getMeasure());
            ingrdientCV.put(RecipesContract.RecentEntry.COLUMN_QUANTITY, ingredientPOJO.getQuantity());
            ingredientsCV[i] = ingrdientCV;
        }
        Log.i(TAG, ingredientsCV.length + " ingredients content values");
        return ingredientsCV;
    }

    public int getNumberOfIngredients() { return ingredients.size();}
    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public Integer getServings() {
        return servings;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(name);
        dest.writeList(ingredients);
        dest.writeList(steps);
        dest.writeValue(servings);
        dest.writeValue(image);
    }

    public int describeContents() {
        return  0;
    }
}
