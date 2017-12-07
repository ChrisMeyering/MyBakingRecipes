package com.baking.chris.mybakingrecipes.data;

/**
 * Created by http://www.jsonschema2pojo.org/ on 11/16/17.
 */

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.baking.chris.mybakingrecipes.provider.RecipesContract;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Ingredient implements Parcelable
{

    @SerializedName("quantity")
    @Expose
    private float quantity;
    @SerializedName("measure")
    @Expose
    private String measure;
    @SerializedName("ingredient")
    @Expose
    private String ingredient;
    public final static Parcelable.Creator<Ingredient> CREATOR = new Creator<Ingredient>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Ingredient createFromParcel(Parcel in) {
            return new Ingredient(in);
        }

        public Ingredient[] newArray(int size) {
            return (new Ingredient[size]);
        }

    }
            ;

    protected Ingredient(Parcel in) {
        this.quantity = ((float) in.readValue((float.class.getClassLoader())));
        this.measure = ((String) in.readValue((String.class.getClassLoader())));
        this.ingredient = ((String) in.readValue((String.class.getClassLoader())));
    }

    public Ingredient() {
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(quantity);
        dest.writeValue(measure);
        dest.writeValue(ingredient);
    }

    public int describeContents() {
        return  0;
    }

    public ContentValues toContentValues() {
        ContentValues ingrdientCV = new ContentValues();
        ingrdientCV.put(RecipesContract.RecentEntry.COLUMN_INGREDIENT, this.getIngredient());
        ingrdientCV.put(RecipesContract.RecentEntry.COLUMN_MEASURE, this.getMeasure());
        ingrdientCV.put(RecipesContract.RecentEntry.COLUMN_QUANTITY, this.getQuantity());
        return ingrdientCV;
    }
}