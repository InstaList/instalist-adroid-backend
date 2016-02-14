package org.noorganization.instalist.provider.internal;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.noorganization.instalist.model.Ingredient;
import org.noorganization.instalist.view.utils.ProviderUtils;
import org.noorganization.instalist.provider.InstalistProvider;
import org.noorganization.instalist.utils.SQLiteUtils;

/**
 * Provider for ingredients.
 * Created by tinos_000 on 13.11.2015.
 */
public class IngredientProvider implements IInternalProvider {

    //region private attributes
    private SQLiteDatabase mDatabase;
    private UriMatcher mMatcher;
    private Context mContext;

    private static final int SINGLE_INGREDIENT = 1;
    private static final int MULTIPLE_INGREDIENT = 2;

    private static final String SINGLE_INGREDIENT_STRING = "ingredient/*";
    private static final String MULTIPLE_INGREDIENT_STRING = "ingredient";

    //endregion private attributes

    //region public attributes

    /**
     * The content uri for actions for a single Ingredient.
     */
    public static final String SINGLE_INGREDIENT_CONTENT_URI = InstalistProvider.BASE_CONTENT_URI + "/" + SINGLE_INGREDIENT_STRING;

    /**
     * The content uri for actions with multiple Ingredients.
     * Does not support {@link android.content.ContentProvider#insert(Uri, ContentValues)},
     * {@link android.content.ContentProvider#update(Uri, ContentValues, String, String[])} ,
     */
    public static final String MULTIPLE_INGREDIENT_CONTENT_URI = InstalistProvider.BASE_CONTENT_URI + "/" + MULTIPLE_INGREDIENT_STRING;

    /**
     * The basic subtype for the {@link IngredientProvider#getType(Uri)}.
     */
    public static final String INGREDIENT_BASE_TYPE = InstalistProvider.BASE_VENDOR + "Ingredient";
    //endregion public attributes

    //region constructors

    /**
     * Constructor of {@link IngredientProvider}
     *
     * @param _context the context of the parent provider. (Needed to notify listener for changes)
     */
    public IngredientProvider(Context _context) {
        mContext = _context;
    }
    //endregion constructors

    //region public overriden methods
    @Override
    public void onCreate(SQLiteDatabase _db) {
        mDatabase = _db;
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        mMatcher.addURI(InstalistProvider.AUTHORITY, MULTIPLE_INGREDIENT_STRING, MULTIPLE_INGREDIENT);
        mMatcher.addURI(InstalistProvider.AUTHORITY, SINGLE_INGREDIENT_STRING, SINGLE_INGREDIENT);
    }

    @Override
    public Cursor query(@NonNull Uri _uri, String[] _projection, String _selection, String[] _selectionArgs, String _sortOrder) {
        Cursor cursor = null;
        switch (mMatcher.match(_uri)) {

            case SINGLE_INGREDIENT:
                String selection = ProviderUtils.prependIdToQuery(Ingredient.COLUMN.ID, _selection);
                String[] selectionArgs = ProviderUtils.prependSelectionArgs(_selectionArgs, _uri.getLastPathSegment());
                cursor = mDatabase.query(Ingredient.TABLE_NAME, _projection, selection, selectionArgs, null, null, _sortOrder);
                break;
            case MULTIPLE_INGREDIENT:
                cursor = mDatabase.query(Ingredient.TABLE_NAME, _projection, _selection, _selectionArgs, null, null, _sortOrder);
                break;
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
            case SINGLE_INGREDIENT:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + INGREDIENT_BASE_TYPE;
            case MULTIPLE_INGREDIENT:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + INGREDIENT_BASE_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(@NonNull Uri _uri, ContentValues _values) {
        Uri newUri = null;
        switch (mMatcher.match(_uri)) {
            case SINGLE_INGREDIENT:
                long rowId = mDatabase.insert(Ingredient.TABLE_NAME, null, _values);
                // insertion went wrong
                if (rowId == -1) {
                    return null;
                    //throw new SQLiteException("Failed to add a record into " + _uri);
                }

                newUri = Uri.parse(SINGLE_INGREDIENT_CONTENT_URI.replace("*",
                        _values.getAsString(Ingredient.COLUMN.ID)));
                break;
            case MULTIPLE_INGREDIENT:
                String newId = SQLiteUtils.generateId(mDatabase, Ingredient.TABLE_NAME).toString();
                _values.put(Ingredient.COLUMN.ID, newId);
                rowId = mDatabase.insert(Ingredient.TABLE_NAME, null, _values);
                // insertion went wrong
                if (rowId == -1) {
                    return null;
                    //throw new SQLiteException("Failed to add a record into " + _uri);
                }

                newUri = Uri.parse(SINGLE_INGREDIENT_CONTENT_URI.replace("*",
                        newId));
                break;
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

        switch (mMatcher.match(_uri)) {
            case SINGLE_INGREDIENT:
                String selection = ProviderUtils.prependIdToQuery(Ingredient.COLUMN.ID, null);
                String[] selectionArgs = ProviderUtils.prependSelectionArgs(null, _uri.getLastPathSegment());
                affectedRows = mDatabase.delete(Ingredient.TABLE_NAME, selection, selectionArgs);
                break;
            case MULTIPLE_INGREDIENT:
                affectedRows = mDatabase.delete(Ingredient.TABLE_NAME, _selection, _selectionArgs);
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
            case SINGLE_INGREDIENT:
                String selection = ProviderUtils.prependIdToQuery(Ingredient.COLUMN.ID, null);
                String[] selectionArgs = ProviderUtils.prependSelectionArgs(null, _uri.getLastPathSegment());
                affectedRows = mDatabase.update(Ingredient.TABLE_NAME, _values, selection, selectionArgs);
                break;
            case MULTIPLE_INGREDIENT:
                // TODO for later purposes maybe
                // affectedRows = mDatabase.update(Ingredient.TABLE_NAME, _values, _selection, _selectionArgs);
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
