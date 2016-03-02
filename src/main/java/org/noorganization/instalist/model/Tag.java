/*
 * Copyright 2016 Tino Siegmund, Michael Wodniok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noorganization.instalist.model;

import android.content.ContentValues;

/**
 * A pseudo-category for products. See {@link org.noorganization.instalist.model.TaggedProduct} for
 * more details.
 * Created by michi on 14.04.15.
 */
public class Tag {

    public final static String TABLE_NAME = "tag";

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
            + ");";

    public String mUUID;

    public String mName;

    // TODO: maybe a frequency parameter to track down usage?

    public Tag() {
        mName = "";
    }

    public Tag(String _name) {
        mName = _name;
    }
    public Tag(String _uuid, String _name) {
        mUUID = _uuid;
        mName = _name;
    }


    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || getClass() != otherObject.getClass()) {
            return false;
        }

        Tag otherTag = (Tag) otherObject;

        return mName.equals(otherTag.mName) && mUUID.compareTo(mUUID) == 0;

    }

    @Override
    public int hashCode() {
        return mUUID.hashCode();
    }

    /**
     * Creates an {@link ContentValues} object that will include each attribute defined.
     *
     * @return the instance related ContentValues.
     */
    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues(2);
        cv.put(COLUMN.ID, this.mUUID);
        cv.put(COLUMN.NAME, this.mName);
        return cv;
    }
}
