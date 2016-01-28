package org.noorganization.instalist.model;

import android.content.ContentValues;

/**
 * Represents a logical recipe. Like ShoppingList, it does not contain a real java list.
 * Created by michi on 14.04.15.
 */
public class Recipe {

    public final static String TABLE_NAME = "recipe";

    /**
     * Column names that does not contain the table prefix.
     */
    public final static class COLUMN {
        public final static String ID = "_id";
        public final static String NAME = "name";
        public final static String[] ALL_COLUMNS = {ID, NAME};
    }

    /**
     * Column names that are prefixed with the table name. So like this TableName.ColumnName
     */
    public final static class COLUMN_PREFIXED {
        public final static String ID = TABLE_NAME.concat("." + COLUMN.ID);
        public final static String NAME = TABLE_NAME.concat("." + COLUMN.NAME);
        public final static String[] ALL_COLUMNS = {ID, NAME};
    }


    public final static String DATABASE_CREATE = "CREATE TABLE " + TABLE_NAME
            + "("
            + COLUMN.ID + " TEXT PRIMARY KEY,"
            + COLUMN.NAME + " TEXT"
            + ")";

    public String mUUID;
    public String mName;

    public Recipe() {
        mName = "";
    }

    public Recipe(String _name) {
        mName = _name;
    }

    public Recipe(String _uuid, String _name){
        mUUID = _uuid;
        mName = _name;
    }
    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues(2);
        cv.put(COLUMN.ID, this.mUUID);
        cv.put(COLUMN.NAME, this.mName);
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

        Recipe anotherRecipe = (Recipe) anotherObject;

        return mName.equals(anotherRecipe.mName) && mUUID.compareTo(anotherRecipe.mUUID) == 0;
    }

    @Override
    public int hashCode() {
        return mUUID.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Recipe { mUUID = ");
        builder.append(mUUID == null ? "null" : mUUID);
        builder.append("; mName = ");
        builder.append(mName == null ? "null" : mName);
        builder.append(" }");
        return builder.toString();
    }
}
