package org.noorganization.instalist.model;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representation of a category.
 * Created by daMihe on 25.05.2015.
 */
public class Category {

    public static final String TABLE_NAME = "category";

    /**
     * Column names that does not contain the table prefix.
     */
    public final static class COLUMN {
        public static final String ID = "_id";
        public static final String NAME = "name";

        public static final String ALL_COLUMNS[] = {ID, NAME};
    }

    /**
     * Column names that are prefixed with the table name. So like this TableName.ColumnName
     */
    public final static class PREFIXED_COLUMN {
        public static final String ID = TABLE_NAME.concat("." + COLUMN.ID);
        public static final String NAME = TABLE_NAME.concat("." + COLUMN.NAME);

        public static final String ALL_COLUMNS[] = {ID, NAME};
    }


    public static final String DB_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN.ID + " TEXT PRIMARY KEY NOT NULL, " +
            COLUMN.NAME + " TEXT NOT NULL)";


    public String mUUID;
    public String mName;

    public Category() {
    }

    public Category(String _uuid, String _name) {
        mUUID = _uuid;
        mName = _name;
    }

    @Override
    public boolean equals(Object _another) {
        if (_another == this) {
            return true;
        }

        if (_another == null || _another.getClass() != getClass()) {
            return false;
        }

        Category anotherCategory = (Category) _another;

        if(anotherCategory.mUUID == null || mUUID == null){
            return false;
        }
        return (mUUID.equals(anotherCategory.mUUID) && mName.equals(anotherCategory.mName));
    }

    @Override
    public int hashCode() {
        return (int) UUID.fromString(mUUID).getLeastSignificantBits();
    }

    public Uri toUri(Uri _baseUri) {
        if (mUUID == null) {
            return null;
        }

        return Uri.withAppendedPath(_baseUri, "category/" + mUUID);
    }
}
