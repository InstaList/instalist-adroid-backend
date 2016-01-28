package org.noorganization.instalist.model;

import android.content.ContentValues;

/**
 * Represents a virtual category entry for a product. Since category usually make people think a
 * thing can only be in one category, the word "tag" is more fitting. For example: A tomato is
 * vegetarian. But it's also lactose-free (or it should be at least).
 * Created by michi on 14.04.15.
 */
public class TaggedProduct {

    public final static String TABLE_NAME = "taggedProduct";

    /**
     * Column names that does not contain the table prefix.
     */
    public final static class COLUMN {

        public final static String ID = "_id";
        public final static String TAG_ID = "tag_id";
        public final static String PRODUCT_ID = "product_id";
        /**
         * Will return all columns of this TaggedProduct Table.
         */
        public final static String[] ALL_COLUMNS = {ID, TAG_ID, PRODUCT_ID};
    }

    /**
     * Column names that are prefixed with the table name. So like this TableName.ColumnName
     */
    public final static class COLUMN_PREFIXED {

        public final static String ID = TABLE_NAME.concat("." + COLUMN.ID);
        public final static String TAG_ID = TABLE_NAME.concat("." + COLUMN.TAG_ID);
        public final static String PRODUCT_ID = TABLE_NAME.concat("." + COLUMN.PRODUCT_ID);

        public final static String[] ALL_COLUMNS = {ID, TAG_ID, PRODUCT_ID};
    }

    /**
     * Returns the column names with qualified table names.
     */
    public final static String[] ALL_COLUMNS_JOINED = {Tag.COLUMN_PREFIXED.ID, Tag.COLUMN_PREFIXED.NAME,
            COLUMN_PREFIXED.ID, COLUMN_PREFIXED.TAG_ID, COLUMN_PREFIXED.PRODUCT_ID,
            Product.PREFIXED_COLUMN.ID, Product.PREFIXED_COLUMN.NAME, Product.PREFIXED_COLUMN.DEFAULT_AMOUNT,
            Product.PREFIXED_COLUMN.STEP_AMOUNT, Product.PREFIXED_COLUMN.UNIT};

    public final static String DATABASE_CREATE = "CREATE TABLE " + TABLE_NAME
            + "("
            + COLUMN.ID + " TEXT PRIMARY KEY,"
            + COLUMN.TAG_ID + " TEXT,"
            + COLUMN.PRODUCT_ID + " TEXT,"
            + "FOREIGN KEY (" + COLUMN.PRODUCT_ID + ") REFERENCES " + Product.TABLE_NAME + " (" + Product.COLUMN.ID + ") "
            + "ON UPDATE CASCADE ON DELETE CASCADE,"
            + "FOREIGN KEY (" + COLUMN.TAG_ID + ") REFERENCES " + Tag.TABLE_NAME + " (" + Tag.COLUMN.ID + ") "
            + "ON UPDATE CASCADE ON DELETE CASCADE"
            + ")";


    public String mUUID;
    public Tag mTag;
    public Product mProduct;

    /**
     * Constructor for initialized to null values.
     */
    public TaggedProduct() {
        mTag = null;
        mProduct = null;
    }

    /**
     * Constructor of TaggedProduct to combine product with a tag.
     *
     * @param _tag     the tag to be assigned.
     * @param _product the product to be connected with the tag.
     */
    public TaggedProduct(Tag _tag, Product _product) {
        mTag = _tag;
        mProduct = _product;
    }

    /**
     * Constructor of TaggedProduct to combine product with a tag and also set a uuid.
     *
     * @param _uuid    the uuid to be assigned.
     * @param _tag     the tag to be assigned.
     * @param _product the product to be connected with the tag.
     */
    public TaggedProduct(String _uuid, Tag _tag, Product _product) {
        mUUID = _uuid;
        mTag = _tag;
        mProduct = _product;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN.ID, this.mUUID);
        cv.put(COLUMN.PRODUCT_ID, this.mProduct != null ? this.mProduct.mUUID : null);
        cv.put(COLUMN.TAG_ID, this.mTag != null ? this.mTag.mUUID : null);
        return cv;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaggedProduct)) return false;

        TaggedProduct that = (TaggedProduct) o;

        if (!mUUID.equals(that.mUUID)) return false;
        if (!mTag.equals(that.mTag)) return false;
        return mProduct.equals(that.mProduct);

    }

    @Override
    public int hashCode() {
        return mUUID.hashCode();
    }
}
