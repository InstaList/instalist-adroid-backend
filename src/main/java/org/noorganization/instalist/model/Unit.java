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

import java.util.UUID;

/**
 * Represents a unit of a product. For example: kilogram
 * Created by michi on 14.04.15.
 */
public class Unit {

    public final static String TABLE_NAME = "unit";

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
    public final static class PREFIXED_COLUMN {
        public final static String ID = TABLE_NAME.concat("." + COLUMN.ID);
        public final static String NAME = TABLE_NAME.concat("." + COLUMN.NAME);
        public final static String[] ALL_COLUMNS = {ID, NAME};
    }


    /**
     * @deprecated do not use anymore instead use {@link COLUMN#NAME}
     */
    public static String ATTR_NAME = "m_name";

    public final static String DATABASE_CREATE = "CREATE TABLE " + TABLE_NAME
            + "("
            + COLUMN.ID + " TEXT PRIMARY KEY, "
            + COLUMN.NAME + " TEXT"
            + ");";

    public String mUUID;
    public String mName;

    public Unit() {
        mName = "";
    }

    public Unit(String _uuid, String _name) {
        mUUID = _uuid;
        mName = _name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Unit anotherUnit = (Unit) other;
        return (((mName == null && anotherUnit.mName == null) ||
                (mName != null && mName.equals(anotherUnit.mName))) &&
                ((mUUID == null && anotherUnit.mUUID == null) ||
                        (mUUID != null && mUUID.equals(anotherUnit.mUUID))));
    }

    @Override
    public int hashCode() {
        if (mUUID == null) {
            return 0;
        }
        return (int) mUUID.hashCode();
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues(2);
        cv.put(COLUMN.ID, this.mUUID);
        cv.put(COLUMN.NAME, this.mName);
        return cv;
    }
}
