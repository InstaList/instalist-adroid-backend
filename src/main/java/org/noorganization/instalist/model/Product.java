package org.noorganization.instalist.model;

import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

/**
 * Represents a product.
 * Created by michi on 14.04.15.
 */
public class Product {

    public final static String TABLE_NAME = "product";

    /**
     * Column names that does not contain the table prefix.
     */
    public final static class COLUMN {

        public final static String ID = "_id";
        public final static String NAME = "name";
        public final static String UNIT = "unit_id";
        public final static String DEFAULT_AMOUNT = "default_amount";
        public final static String STEP_AMOUNT = "step_amount";

        public final static String[] ALL_COLUMNS = new String[]{
                ID,
                NAME,
                UNIT,
                DEFAULT_AMOUNT,
                STEP_AMOUNT
        };
    }

    /**
     * Column names that are prefixed with the table name. So like this TableName.ColumnName
     */
    public final static class PREFIXED_COLUMN {
        public final static String ID = TABLE_NAME.concat("." + Product.COLUMN.ID);
        public final static String NAME = TABLE_NAME.concat("." + Product.COLUMN.NAME);
        public final static String UNIT = TABLE_NAME.concat("." + Product.COLUMN.UNIT);
        public final static String DEFAULT_AMOUNT = TABLE_NAME.concat("." + Product.COLUMN.DEFAULT_AMOUNT);
        public final static String STEP_AMOUNT = TABLE_NAME.concat("." + Product.COLUMN.STEP_AMOUNT);

        public final static String[] ALL_COLUMNS = new String[]{
                ID,
                NAME,
                UNIT,
                DEFAULT_AMOUNT,
                STEP_AMOUNT
        };
    }

    public static final class DEFAULTS {
        public static final float DEFAULT_AMOUNT = 1.0f;
        public static final float STEP_AMOUNT = 1.0f;
    }

    public final static String DATABASE_CREATE = "CREATE TABLE " + TABLE_NAME
            + "(" + Product.COLUMN.ID + " TEXT PRIMARY KEY NOT NULL,"
            + Product.COLUMN.NAME + " TEXT NOT NULL,"
            + Product.COLUMN.UNIT + " TEXT,"
            + Product.COLUMN.DEFAULT_AMOUNT + " REAL DEFAULT " + DEFAULTS.DEFAULT_AMOUNT + ","
            + Product.COLUMN.STEP_AMOUNT + " REAL DEFAULT " + DEFAULTS.STEP_AMOUNT + ","
            + "FOREIGN KEY ( " + Product.COLUMN.UNIT + ") REFERENCES " + Unit.TABLE_NAME + "(" + Unit.COLUMN.ID + ")"
            + "ON DELETE SET NULL ON UPDATE NO ACTION"
            + ");";

    public String mUUID;

    public String mName;
    /**
     * The unit of the product. Can also be null if the products has no unit.
     */
    public Unit mUnit;
    /**
     * The default amount is usually 1.0f
     */
    public float mDefaultAmount;
    /**
     * The amount to increase or decrease over quick buttons. Usually 1.0f.
     */
    public float mStepAmount;

    public Product() {
        mUnit = null;
        mName = "";
        mDefaultAmount = DEFAULTS.DEFAULT_AMOUNT;
        mStepAmount = DEFAULTS.STEP_AMOUNT;
    }

    public Product(String _name, Unit _unit, float _defaultAmount, float _stepAmount) {
        mUnit = _unit;
        mName = _name;
        mDefaultAmount = _defaultAmount;
        mStepAmount = _stepAmount;
    }

    public Product(String _uuid, String _name, Unit _unit, float _defaultAmount, float _stepAmount) {
        mUUID = _uuid;
        mUnit = _unit;
        mName = _name;
        mDefaultAmount = _defaultAmount;
        mStepAmount = _stepAmount;
    }

    public Product(String _name, Unit _unit) {
        mUnit = _unit;
        mName = _name;
        mDefaultAmount = DEFAULTS.DEFAULT_AMOUNT;
        mStepAmount = DEFAULTS.STEP_AMOUNT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Product anotherProduct = (Product) o;

        if (Float.compare(anotherProduct.mDefaultAmount, mDefaultAmount) != 0) {
            Log.d("Product", "Equals failed: different default amount");
            return false;
        }
        if (Float.compare(anotherProduct.mStepAmount, mStepAmount) != 0) {
            Log.d("Product", "Equals failed: different step amount");
            return false;
        }
        if ((mName == null && anotherProduct.mName != null) ||
                (mName != null && !mName.equals(anotherProduct.mName))) {
            Log.d("Product", "Equals failed: different name");
            return false;
        }
        if ((mUnit == null && anotherProduct.mUnit != null) ||
                (mUnit != null && !mUnit.equals(anotherProduct.mUnit))) {
            Log.d("Product", "Equals failed: different unit");
            return false;
        }
        if ((mUUID == null && anotherProduct.mUUID != null) ||
                (mUUID != null && !mUUID.equals(anotherProduct.mUUID))) {
            Log.d("Product", "Equals failed: different uuid");
            return false;
        }

        return true;
    }


    @Override
    public int hashCode() {
        return mUUID.hashCode();
    }

    @Override
    public String toString() {
        return "Product{" +
                "mUUID='" + mUUID + '\'' +
                "mName='" + mName + '\'' +
                ", mUnit=" + (mUnit == null ? "null" : "id:" + mUnit.mUUID) +
                ", mDefaultAmount=" + mDefaultAmount +
                ", mStepAmount=" + mStepAmount +
                '}';
    }

    public Uri toUri(Uri _baseUri) {
        if (mUUID == null) {
            return null;
        }

        return Uri.withAppendedPath(_baseUri, "product/" + mUUID);
    }

    /**
     * Creates a {@link ContentValues} Object with all column fields of this class.
     *
     * @return the contentvalues for this instance.
     */
    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues(5);
        cv.put(COLUMN.ID, this.mUUID);
        cv.put(COLUMN.NAME, this.mName);
        cv.put(COLUMN.DEFAULT_AMOUNT, this.mDefaultAmount);
        cv.put(COLUMN.STEP_AMOUNT, this.mStepAmount);
        cv.put(COLUMN.UNIT, this.mUnit == null ? "-" : this.mUnit.mUUID);
        return cv;
    }
}
