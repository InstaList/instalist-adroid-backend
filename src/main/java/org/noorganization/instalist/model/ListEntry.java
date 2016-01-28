package org.noorganization.instalist.model;

import android.net.Uri;
import android.util.Log;

/**
 * Represents a product written on a shoppinglist.
 * Created by michi on 14.04.15.
 */
public class ListEntry {

    public static final String TABLE_NAME = "listentry";


    /**
     * Column names that does not contain the table prefix.
     */
    public final static class COLUMN {
        public static final String ID = "_id";
        public static final String AMOUNT = "amount";
        public static final String LIST = "list";
        public static final String PRIORITY = "priority";
        public static final String PRODUCT = "product";
        public static final String STRUCK = "struck";

        public static final String ALL_COLUMNS[] = {ID, AMOUNT, LIST, PRIORITY, PRODUCT, STRUCK};
    }

    /**
     * Column names that are prefixed with the table name. So like this TableName.ColumnName
     */
    public final static class PREFIXED_COLUMN {
        public static final String ID = TABLE_NAME.concat("." + COLUMN.ID);
        public static final String AMOUNT = TABLE_NAME.concat("." + COLUMN.AMOUNT);
        public static final String LIST = TABLE_NAME.concat("." + COLUMN.LIST);
        public static final String PRIORITY = TABLE_NAME.concat("." + COLUMN.PRIORITY);
        public static final String PRODUCT = TABLE_NAME.concat("." + COLUMN.PRODUCT);
        public static final String STRUCK = TABLE_NAME.concat("." + COLUMN.STRUCK);

        public static final String ALL_COLUMNS[] = {ID, AMOUNT, LIST, PRIORITY, PRODUCT, STRUCK};
    }

    public static final class DEFAULTS {
        public static final float AMOUNT = 1.0f;
        public static final int PRIORITY = 0;
        public static final int STRUCK = 0;
    }


    public static final String DB_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN.ID + " TEXT PRIMARY KEY NOT NULL, " +
            COLUMN.AMOUNT + " REAL NOT NULL DEFAULT " + DEFAULTS.AMOUNT + ", " +
            COLUMN.PRIORITY + " INTEGER NOT NULL DEFAULT " + DEFAULTS.PRIORITY + ", " +
            COLUMN.PRODUCT + " TEXT NOT NULL, " +
            COLUMN.LIST + " TEXT NOT NULL, " +
            COLUMN.STRUCK + " INTEGER NOT NULL DEFAULT " + DEFAULTS.STRUCK + ", " +
            "FOREIGN KEY (" + COLUMN.PRODUCT + ") REFERENCES " + Product.TABLE_NAME + " (" +
            Product.COLUMN.ID + ") ON UPDATE CASCADE ON DELETE CASCADE, " +
            "FOREIGN KEY (list) REFERENCES list (_id) ON UPDATE CASCADE ON DELETE CASCADE)";

    public String mUUID;
    public ShoppingList mList;
    public Product mProduct;
    /**
     * The amount of product that's listed
     */
    public float mAmount;
    /**
     * Whether a product is struck through, i.e. because it's already bought.
     */
    public boolean mStruck;
    public int mPriority;

    public ListEntry() {
        mList = null;
        mProduct = null;
        mAmount = DEFAULTS.AMOUNT;
        mStruck = (DEFAULTS.STRUCK != 0);
        mPriority = DEFAULTS.PRIORITY;
    }


    public ListEntry(ShoppingList _list, Product _product, float _amount) {
        mList = _list;
        mProduct = _product;
        mAmount = _amount;
        mStruck = (DEFAULTS.STRUCK != 0);
        mPriority = DEFAULTS.PRIORITY;
    }

    public ListEntry(ShoppingList _list, Product _product, float _amount, boolean _struck) {
        mList = _list;
        mProduct = _product;
        mAmount = _amount;
        mStruck = _struck;
        mPriority = DEFAULTS.PRIORITY;
    }

    public ListEntry(ShoppingList _list, Product _product, float _amount, boolean _struck, int _prio) {
        mList = _list;
        mProduct = _product;
        mAmount = _amount;
        mStruck = _struck;
        mPriority = _prio;
    }

    public ListEntry(String _uuid, ShoppingList _list, Product _product, float _amount, boolean _struck, int _prio) {
        mUUID = _uuid;
        mList = _list;
        mProduct = _product;
        mAmount = _amount;
        mStruck = _struck;
        mPriority = _prio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ListEntry that = (ListEntry) o;

        if ((mUUID == null && that.mUUID != null) ||
                (mUUID != null && !mUUID.equals(that.mUUID))) {
            Log.d("ListEntry", "Equals failed: different uuid");
            return false;
        }
        if (Float.compare(that.mAmount, mAmount) != 0) {
            Log.d("ListEntry", "Equals failed: different amount");
            return false;
        }
        if (mStruck != that.mStruck) {
            Log.d("ListEntry", "Equals failed: different struck-status");
            return false;
        }
        if ((mList == null && that.mList != null) ||
                (mList != null && !mList.equals(that.mList))) {
            Log.d("ListEntry", "Equals failed: different list");
            return false;
        }
        if ((mProduct == null && that.mProduct != null) ||
                (mProduct != null && !mProduct.equals(that.mProduct))) {
            Log.d("ListEntry", "Equals failed: different product");
            return false;
        }
        if (mPriority != that.mPriority) {
            Log.d("ListEntry", "Equals failed: different priority");
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
        return "ListEntry { id = " + mUUID + ", mList.id = " + (mList == null ? "none" :
                mList.mUUID) +
                ", mProduct.id = " + (mProduct == null ? "none" : mProduct.mUUID) +
                ", mStruck = " + mStruck + ", mAmount = " + mAmount + ", mPriority = " + mPriority +
                " }";
    }

    public Uri toUri(Uri _baseUri) {
        if (mList == null || mUUID == null) {
            return null;
        }

        return Uri.withAppendedPath(_baseUri, "category/" +
                (mList.mCategory == null ? "-" : mList.mCategory.mUUID) + "/list/" +
                mList.mUUID + "/entry/" + mUUID);
    }
}
