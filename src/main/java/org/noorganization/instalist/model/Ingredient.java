package org.noorganization.instalist.model;

import android.content.ContentValues;

/**
 * Represents a product as ingredient in a recipe.
 * Created by michi on 14.04.15.
 */
public class Ingredient {

    public final static String TABLE_NAME = "ingredient";

    /**
     * Column names that does not contain the table prefix.
     */
    public final static class COLUMN {
        public final static String ID = "_id";
        public final static String PRODUCT_ID = "product_id";
        public final static String RECIPE_ID = "recipe_id";
        public final static String AMOUNT = "amount";
        public final static String[] ALL_COLUMNS = {ID, PRODUCT_ID, RECIPE_ID, AMOUNT};
    }

    /**
     * Column names that are prefixed with the table name. So like this TableName.ColumnName
     */
    public final static class COLUMN_PREFIXED {
        public final static String ID = TABLE_NAME.concat(Ingredient.COLUMN.ID);
        public final static String PRODUCT_ID = TABLE_NAME.concat(Ingredient.COLUMN.PRODUCT_ID);
        public final static String RECIPE_ID = TABLE_NAME.concat(Ingredient.COLUMN.RECIPE_ID);
        public final static String AMOUNT = TABLE_NAME.concat(Ingredient.COLUMN.AMOUNT);
        public final static String[] ALL_COLUMNS = {ID, PRODUCT_ID, RECIPE_ID, AMOUNT};
    }


    public final static String DATABASE_CREATE = "CREATE TABLE " + TABLE_NAME
            + "("
            + Ingredient.COLUMN.ID + " TEXT PRIMARY KEY,"
            + Ingredient.COLUMN.PRODUCT_ID + " TEXT,"
            + Ingredient.COLUMN.RECIPE_ID + " TEXT,"
            + Ingredient.COLUMN.AMOUNT + " REAL,"
            + "FOREIGN KEY (" + Ingredient.COLUMN.PRODUCT_ID + ") REFERENCES " + Product.TABLE_NAME + "( " + Product.COLUMN.ID + " )"
            + "ON UPDATE CASCADE ON DELETE CASCADE,"
            + "FOREIGN KEY (" + Ingredient.COLUMN.RECIPE_ID + ") REFERENCES " + Recipe.TABLE_NAME + "( " + Recipe.COLUMN.ID + " )"
            + "ON UPDATE CASCADE ON DELETE CASCADE"
            + ")";

    public String mUUID;
    public Product mProduct;
    public Recipe mRecipe;
    public float mAmount;

    public Ingredient() {
        mProduct = null;
        mRecipe = null;
        mAmount = 1.0f;
    }

    public Ingredient(Product _product, Recipe _recipe, float _amount) {
        mProduct = _product;
        mRecipe = _recipe;
        mAmount = _amount;
    }

    public Ingredient(String _uuid, Product _product, Recipe _recipe, float _amount) {
        mUUID = _uuid;
        mProduct = _product;
        mRecipe = _recipe;
        mAmount = _amount;
    }
    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues(4);
        cv.put(COLUMN.ID, this.mUUID);
        cv.put(COLUMN.PRODUCT_ID, this.mProduct.mUUID);
        cv.put(COLUMN.RECIPE_ID, this.mRecipe.mUUID);
        cv.put(COLUMN.AMOUNT, this.mAmount);
        return cv;
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (this == anotherObject) {
            return true;
        }
        if (anotherObject == null || getClass() != anotherObject.getClass()) {
            return false;
        }

        Ingredient anotherIngredient = (Ingredient) anotherObject;

        if (Float.compare(anotherIngredient.mAmount, mAmount) != 0) {
            return false;
        }
        if (!mProduct.equals(anotherIngredient.mProduct)) {
            return false;
        }
        if (!mRecipe.equals(anotherIngredient.mRecipe)) {
            return false;
        }
        return mUUID.compareTo(anotherIngredient.mUUID) == 0;

    }

    @Override
    public int hashCode() {
        return mUUID.hashCode();
    }
}
