package org.noorganization.instalist.provider.internal;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.Tag;
import org.noorganization.instalist.model.TaggedProduct;
import org.noorganization.instalist.view.utils.ProviderUtils;
import org.noorganization.instalist.provider.InstalistProvider;
import org.noorganization.instalist.utils.SQLiteUtils;

import java.util.List;

/**
 * The TaggedProductProvider provides the interaction with products which are tagged. This means you
 * can add a tag to a product or simply query a list of products by a custom tag. It provides a
 * huge variety of dynamic.
 * <p/>
 * Created by Tino on 26.10.2015.
 */
public class TaggedProductProvider implements IInternalProvider {

    //region private attributes
    private SQLiteDatabase mDatabase;
    private UriMatcher mMatcher;
    private Context mContext;


    private static final int SINGLE_TAGGED_PRODUCT = 1;
    private static final int MULTIPLE_TAGGED_PRODUCTS = 2;
    private static final int SINGLE_TAGGED_PRODUCT_BY_TAG = 3;
    private static final int MULTIPLE_TAGGED_PRODUCT_BY_TAG = 4;

    private static final String SINGLE_TAGGED_PRODUCT_STRING = "taggedProduct/*";
    private static final String MULTIPLE_TAGGED_PRODUCT_STRING = "taggedProduct";

    private static final String SINGLE_TAGGED_PRODUCT_BY_TAG_STRING = "taggedProduct/*/tag/*";
    private static final String MULTIPLE_TAGGED_PRODUCT_BY_TAG_STRING = "taggedProduct/tag/*";

    //endregion private attributes

    //region public attributes
    /**
     * The content uri for actions for a single {@link TaggedProduct}
     */
    public static final String SINGLE_TAGGED_PRODUCT_CONTENT_URI = InstalistProvider.BASE_CONTENT_URI + "/" + SINGLE_TAGGED_PRODUCT_STRING;

    /**
     * The content uri for actions with multiple {@link TaggedProduct}s.
     * Supports not {@link android.content.ContentProvider#update(Uri, ContentValues, String, String[])}
     * and {@link android.content.ContentProvider#insert(Uri, ContentValues)}
     */
    public static final String MULTIPLE_TAGGED_PRODUCT_CONTENT_URI = InstalistProvider.BASE_CONTENT_URI + "/" + MULTIPLE_TAGGED_PRODUCT_STRING;

    /**
     * The content uri for actions with single {@link TaggedProduct} especially for actions to tag a product.
     */
    public static final String SINGLE_TAGGED_PRODUCT_BY_TAG_CONTENT_URI = InstalistProvider.BASE_CONTENT_URI + "/" + SINGLE_TAGGED_PRODUCT_BY_TAG_STRING;

    /**
     * The content uri for actions with multiple {@link TaggedProduct} especially for actions to query multiple products by a tag.
     * Supports not {@link android.content.ContentProvider#update(Uri, ContentValues, String, String[])}
     * and {@link android.content.ContentProvider#insert(Uri, ContentValues)}
     */
    public static final String MULTIPLE_TAGGED_PRODUCT_BY_TAG_CONTENT_URI = InstalistProvider.BASE_CONTENT_URI + "/" + MULTIPLE_TAGGED_PRODUCT_BY_TAG_STRING;

    /**
     * The basic subtype for the {@link TaggedProductProvider#getType(Uri)}.
     */
    public static final String TAGGED_PRODUCT_BASE_TYPE = InstalistProvider.BASE_VENDOR + "taggedProduct";
    //endregion public attributes

    //region constructors

    /**
     * Constructor of {@link ProductProvider}
     *
     * @param _context the context of the parent provider. (Needed to notify listener for changes)
     */
    public TaggedProductProvider(Context _context) {
        mContext = _context;
    }
    //endregion constructors

    //region public overriden methods
    @Override
    public void onCreate(SQLiteDatabase _db) {
        mDatabase = _db;
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mMatcher.addURI(InstalistProvider.AUTHORITY, MULTIPLE_TAGGED_PRODUCT_STRING, MULTIPLE_TAGGED_PRODUCTS);
        mMatcher.addURI(InstalistProvider.AUTHORITY, MULTIPLE_TAGGED_PRODUCT_BY_TAG_STRING, MULTIPLE_TAGGED_PRODUCT_BY_TAG);
        mMatcher.addURI(InstalistProvider.AUTHORITY, SINGLE_TAGGED_PRODUCT_STRING, SINGLE_TAGGED_PRODUCT);
        mMatcher.addURI(InstalistProvider.AUTHORITY, SINGLE_TAGGED_PRODUCT_BY_TAG_STRING, SINGLE_TAGGED_PRODUCT_BY_TAG);
    }

    @Override
    public Cursor query(@NonNull Uri _uri, String[] _projection, String _selection, String[] _selectionArgs, String _sortOrder) {
        Cursor cursor = null;
        // projection is null so there will be no data be fetched
        if (_projection == null || _projection.length == 0) {
            return null;
        }

        switch (mMatcher.match(_uri)) {

            case SINGLE_TAGGED_PRODUCT:
                String sql = "SELECT " + TextUtils.join(",", _projection);
                sql += " FROM " + TaggedProduct.TABLE_NAME + " INNER JOIN " + Tag.TABLE_NAME + " ON "
                        + TaggedProduct.COLUMN_PREFIXED.TAG_ID + "=" + Tag.COLUMN_PREFIXED.ID;
                sql += " INNER JOIN " + Product.TABLE_NAME + " ON " + TaggedProduct.COLUMN_PREFIXED.PRODUCT_ID + " = " + Product.PREFIXED_COLUMN.ID;
                sql += " WHERE " + TaggedProduct.COLUMN_PREFIXED.ID + "=\"" + _uri.getLastPathSegment() + "\"";
                if (_selection != null && _selection.length() > 0) {
                    sql += " AND ";
                    sql += String.format(_selection, (Object[])_selectionArgs);
                }

                if (_sortOrder != null) {
                    sql += " ORDER BY " + _sortOrder;
                }
                cursor = mDatabase.rawQuery(sql, null);
                break;
            case MULTIPLE_TAGGED_PRODUCTS:
                sql = "SELECT " + TextUtils.join(",", _projection);
                sql += " FROM " + TaggedProduct.TABLE_NAME + " INNER JOIN " + Tag.TABLE_NAME + " ON "
                        + TaggedProduct.COLUMN_PREFIXED.TAG_ID + "=" + Tag.COLUMN_PREFIXED.ID;
                sql += " INNER JOIN " + Product.TABLE_NAME + " ON " + TaggedProduct.COLUMN_PREFIXED.PRODUCT_ID + " = " + Product.PREFIXED_COLUMN.ID;
                if (_selection != null && _selection.length() > 0) {
                    sql += " AND ";
                    sql += String.format(_selection, (Object[])_selectionArgs);
                }

                if (_sortOrder != null) {
                    sql += " ORDER BY " + _sortOrder;
                }
                cursor = mDatabase.rawQuery(sql, null);
                break;
            case MULTIPLE_TAGGED_PRODUCT_BY_TAG:
                String tagId = _uri.getLastPathSegment();
                sql = "SELECT " + TextUtils.join(",", _projection);
                sql += " FROM " + TaggedProduct.TABLE_NAME + " INNER JOIN " + Tag.TABLE_NAME + " ON "
                        + TaggedProduct.COLUMN_PREFIXED.TAG_ID + "=" + Tag.COLUMN_PREFIXED.ID;
                sql += " INNER JOIN " + Product.TABLE_NAME + " ON " + TaggedProduct.COLUMN_PREFIXED.PRODUCT_ID + "=" + Product.PREFIXED_COLUMN.ID;
                sql += " WHERE " + TaggedProduct.COLUMN_PREFIXED.TAG_ID + "=\"" + tagId + "\"";
                if (_selection != null && _selection.length() > 0) {
                    sql += " AND ";
                    sql += String.format(_selection, (Object[])_selectionArgs);
                }
                if (_sortOrder != null) {
                    sql += " ORDER BY " + _sortOrder;
                }

                cursor = mDatabase.rawQuery(sql, null);

                break;
            case SINGLE_TAGGED_PRODUCT_BY_TAG:
            default:
                throw new IllegalArgumentException("The given Uri is not supported: " + _uri);
        }

        if (cursor != null) {
            // notify all possibles listener
            cursor.setNotificationUri(mContext.getContentResolver(), _uri);
        }

        return cursor;
    }

    @Override
    public String getType(@NonNull Uri _uri) {
        switch (mMatcher.match(_uri)) {
            case SINGLE_TAGGED_PRODUCT:
            case SINGLE_TAGGED_PRODUCT_BY_TAG:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + TAGGED_PRODUCT_BASE_TYPE;
            case MULTIPLE_TAGGED_PRODUCTS:
            case MULTIPLE_TAGGED_PRODUCT_BY_TAG:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + TAGGED_PRODUCT_BASE_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(@NonNull Uri _uri, ContentValues _values) {
        Uri newUri = null;
        switch (mMatcher.match(_uri)) {
            case SINGLE_TAGGED_PRODUCT:
            case SINGLE_TAGGED_PRODUCT_BY_TAG:
                long rowId = mDatabase.insert(TaggedProduct.TABLE_NAME, null, _values);
                // insertion went wrong
                if (rowId == -1) {
                    return null;
                    //throw new SQLiteException("Failed to add a record into " + _uri);
                }
                Cursor cursor = mDatabase.query(TaggedProduct.TABLE_NAME, new String[]{TaggedProduct.COLUMN_PREFIXED.ID},
                        SQLiteUtils.COLUMN_ROW_ID + "=?", new String[]{String.valueOf(rowId)},
                        null, null, null, null);
                cursor.moveToFirst();
                newUri = Uri.parse(SINGLE_TAGGED_PRODUCT_CONTENT_URI.replace("*",
                        cursor.getString(cursor.getColumnIndex(TaggedProduct.COLUMN_PREFIXED.ID))));
                break;
            case MULTIPLE_TAGGED_PRODUCTS:
                _values.put(TaggedProduct.COLUMN.ID, SQLiteUtils.generateId(mDatabase, TaggedProduct.TABLE_NAME).toString());
                rowId = mDatabase.insert(TaggedProduct.TABLE_NAME, null, _values);
                // insertion went wrong
                if (rowId == -1) {
                    return null;
                    //throw new SQLiteException("Failed to add a record into " + _uri);
                }
                newUri = Uri.parse(SINGLE_TAGGED_PRODUCT_CONTENT_URI.replace("*",
                        _values.getAsString(TaggedProduct.COLUMN.ID)));
                break;
            case MULTIPLE_TAGGED_PRODUCT_BY_TAG:
            default:
                throw new IllegalArgumentException("The given Uri is not supported: " + _uri);
        }

        if (newUri != null) {
            // notify all listener that the cursor data has changed!
            mContext.getContentResolver().notifyChange(_uri, null);
        }
        return newUri;
    }

    @Override
    public int delete(@NonNull Uri _uri, String _selection, String[] _selectionArgs) {
        int affectedRows = 0;
        List<String> pathSegments = _uri.getPathSegments();
        switch (mMatcher.match(_uri)) {
            case SINGLE_TAGGED_PRODUCT:
                String selection = ProviderUtils.prependIdToQuery(TaggedProduct.COLUMN_PREFIXED.ID, null);
                String[] selectionArgs = ProviderUtils.prependSelectionArgs(null, _uri.getLastPathSegment());
                affectedRows = mDatabase.delete(TaggedProduct.TABLE_NAME, selection, selectionArgs);
                break;
            case MULTIPLE_TAGGED_PRODUCTS:
                affectedRows = mDatabase.delete(TaggedProduct.TABLE_NAME, _selection, _selectionArgs);
                break;
            case SINGLE_TAGGED_PRODUCT_BY_TAG:
                String tagged_id = pathSegments.get(1);
                String tag_id = pathSegments.get(3);
                selection = ProviderUtils.prependIdToQuery(TaggedProduct.COLUMN_PREFIXED.ID, _selection);
                selection = ProviderUtils.prependIdToQuery(TaggedProduct.COLUMN_PREFIXED.TAG_ID, selection);
                selectionArgs = ProviderUtils.prependSelectionArgs(_selectionArgs, tagged_id);
                selectionArgs = ProviderUtils.prependSelectionArgs(_selectionArgs, tag_id);

                affectedRows = mDatabase.delete(TaggedProduct.TABLE_NAME, selection, selectionArgs);
                break;
            case MULTIPLE_TAGGED_PRODUCT_BY_TAG:
                tag_id = _uri.getLastPathSegment();
                selection = ProviderUtils.prependIdToQuery(TaggedProduct.COLUMN_PREFIXED.TAG_ID, _selection);
                selectionArgs = ProviderUtils.prependSelectionArgs(_selectionArgs, tag_id);
                affectedRows = mDatabase.delete(TaggedProduct.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("The given Uri is not supported: " + _uri);
        }
        if (affectedRows > 0) {
            // notify that the dataset has changed
            mContext.getContentResolver().notifyChange(_uri, null);
        }
        return affectedRows;
    }

    @Override
    public int update(@NonNull Uri _uri, ContentValues _values, String _selection, String[] _selectionArgs) {
        int affectedRows = 0;
        switch (mMatcher.match(_uri)) {
            case SINGLE_TAGGED_PRODUCT:
                String selection = ProviderUtils.prependIdToQuery(TaggedProduct.COLUMN_PREFIXED.ID, null);
                String[] selectionArgs = ProviderUtils.prependSelectionArgs(null, _uri.getLastPathSegment());
                affectedRows = mDatabase.update(TaggedProduct.TABLE_NAME, _values, selection, selectionArgs);
                break;
            case SINGLE_TAGGED_PRODUCT_BY_TAG:
                List<String> pathSegments = _uri.getPathSegments();
                selection = ProviderUtils.prependIdToQuery(TaggedProduct.COLUMN_PREFIXED.PRODUCT_ID, _selection);
                selection = ProviderUtils.prependIdToQuery(TaggedProduct.COLUMN_PREFIXED.TAG_ID, _selection);
                selectionArgs = ProviderUtils.prependSelectionArgs(_selectionArgs, pathSegments.get(1));
                selectionArgs = ProviderUtils.prependSelectionArgs(_selectionArgs, pathSegments.get(3));

                affectedRows = mDatabase.update(TaggedProduct.TABLE_NAME, _values, selection, selectionArgs);
                break;
            case MULTIPLE_TAGGED_PRODUCT_BY_TAG:
            case MULTIPLE_TAGGED_PRODUCTS:
            default:
                throw new IllegalArgumentException("The given Uri is not supported: " + _uri);
        }

        if (affectedRows > 0) {
            // notify that the content has changed
            mContext.getContentResolver().notifyChange(_uri, null);
        }
        return affectedRows;
    }

    //endregion overwritten methods

}
