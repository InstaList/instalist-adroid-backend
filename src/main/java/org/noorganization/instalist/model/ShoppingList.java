package org.noorganization.instalist.model;

import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import java.util.UUID;

/**
 * Represents a shoppinglist itself as a logical object. This object does not contain a java list.
 * Created by michi on 14.04.15.
 */
public class ShoppingList {

    public static final String TABLE_NAME = "list";

    /**
     * Column names that does not contain the table prefix.
     */
    public final static class COLUMN {
        public static final String ID = "_id";
        public static final String NAME = "name";
        public static final String CATEGORY = "category";

        public static final String ALL_COLUMNS[] = {ID, NAME, CATEGORY};
    }

    /**
     * Column names that are prefixed with the table name. So like this TableName.ColumnName
     */
    public final static class PREFIXED_COLUMN {
        public static final String ID = TABLE_NAME.concat("." + COLUMN.ID);
        public static final String NAME = TABLE_NAME.concat("." + COLUMN.NAME);
        public static final String CATEGORY = TABLE_NAME.concat("." + COLUMN.CATEGORY);
        public static final String ALL_COLUMNS[] = {ID, NAME, CATEGORY};
    }


    public static final String DB_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN.ID + " TEXT PRIMARY KEY NOT NULL, " +
            COLUMN.NAME + " TEXT NOT NULL, " +
            COLUMN.CATEGORY + " TEXT, " +
            "FOREIGN KEY (" + COLUMN.CATEGORY + ") REFERENCES " + Category.TABLE_NAME +
            " (" + Category.COLUMN.ID + ") ON UPDATE CASCADE ON DELETE CASCADE)";

    public String mUUID;
    public String mName;
    public Category mCategory;

    public ShoppingList() {
        mUUID = null;
        mName = "";
        mCategory = null;
    }

    public ShoppingList(String _uuid, String _name) {
        mUUID = _uuid;
        mName = _name;
        mCategory = null;
    }

    public ShoppingList(String _uuid, String _name, Category _category) {
        mUUID = _uuid;
        mName = _name;
        mCategory = _category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ShoppingList that = (ShoppingList) o;

        if ((mCategory == null && that.mCategory != null) ||
                (mCategory != null && !mCategory.equals(that.mCategory))) {
            Log.d("ShoppingList", "Equals failed: different category");
            return false;
        }
        if ((mName == null && that.mName != null) ||
                (mName != null && !mName.equals(that.mName))) {
            Log.d("ShoppingList", "Equals failed: different name");
            return false;
        }
        if ((mUUID == null && that.mUUID != null) ||
                (mUUID != null && !mUUID.equals(that.mUUID))) {
            Log.d("ShoppingList", "Equals failed: different uuid");
            return false;
        }

        return true;

    }

    @Override
    public int hashCode() {
        if (mUUID == null) {
            return 0;
        }
        return (int) UUID.fromString(mUUID).getLeastSignificantBits();
    }

    /**
     * Creates a full qualified URI to accces the list ressource with the content resolver for the provider {@link org.noorganization.instalist.provider.InstalistProvider}.
     *
     * @param _baseUri the base url of the provider.
     * @return null if uuid is null else the uri to the list object.
     */
    public Uri toUri(Uri _baseUri) {
        if (mUUID == null) {
            return null;
        }

        return Uri.withAppendedPath(_baseUri, getUriPath());
    }

    public String getUriPath() {
        if (mUUID == null) {
            return null;
        }

        return "category/" + (mCategory == null ? "-" : mCategory.mUUID) + "/list/" + mUUID;
    }

    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues(3);
        contentValues.put(COLUMN.ID, this.mUUID);
        contentValues.put(COLUMN.NAME, this.mName);
        contentValues.put(COLUMN.CATEGORY, this.mCategory.mUUID);
        return contentValues;
    }

}
