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
import org.noorganization.instalist.model.Recipe;
import org.noorganization.instalist.provider.InstalistProvider;
import org.noorganization.instalist.utils.SQLiteUtils;
import org.noorganization.instalist.view.utils.ProviderUtils;

/**
 * // TODO in train :)
 * Created by Tino on 26.10.2015.
 */
public class RecipeProvider implements IInternalProvider {
    //region private attributes
    private SQLiteDatabase mDatabase;
    private UriMatcher mMatcher;
    private Context mContext;

    private static final int SINGLE_RECIPE = 1;
    private static final int MULTIPLE_RECIPES = 2;
    private static final int SINGLE_RECIPE_INGREDIENT = 3;
    private static final int MULTIPLE_RECIPE_INGREDIENT = 4;


    private static final String SINGLE_RECIPE_STRING = "recipe/*";
    private static final String MULTIPLE_RECIPE_STRING = "recipe";
    private static final String SINGLE_RECIPE_INGREDIENT_STRING = "recipe/*/ingredient/*";
    private static final String MULTIPLE_RECIPE_INGREDIENT_STRING = "recipe/*/ingredient";

    //endregion private attributes

    //region public attributes

    /**
     * The content uri for actions for a single unit.
     */
    public static final String SINGLE_RECIPE_CONTENT_URI = InstalistProvider.BASE_CONTENT_URI + "/" + SINGLE_RECIPE_STRING;
    /**
     * The content uri for actions with multiple unit.
     */
    public static final String MULTIPLE_RECIPE_CONTENT_URI = InstalistProvider.BASE_CONTENT_URI + "/" + MULTIPLE_RECIPE_STRING;

    /**
     * The content uri for actions for a single ingredient.
     */
    public static final String SINGLE_RECIPE_INGREDIENT_CONTENT_URI = InstalistProvider.BASE_CONTENT_URI + "/" + SINGLE_RECIPE_INGREDIENT_STRING;
    /**
     * The content uri for actions with multiple ingredient.
     */
    public static final String MULTIPLE_RECIPE_INGREDIENT_CONTENT_URI = InstalistProvider.BASE_CONTENT_URI + "/" + MULTIPLE_RECIPE_INGREDIENT_STRING;

    /**
     * The basic subtype of recipe for the {@link UnitProvider#getType(Uri)}.
     */
    public static final String RECIPE_BASE_TYPE = InstalistProvider.BASE_VENDOR + "recipe";

    /**
     * The basic subtype of ingredient for the {@link UnitProvider#getType(Uri)}.
     */
    public static final String INGREDIENT_BASE_TYPE = InstalistProvider.BASE_VENDOR + "ingredient";

    //endregion public attributes

    //region constructors

    /**
     * Constructor of {@link UnitProvider}
     *
     * @param _context the context of the parent provider. (Needed to notify listener for changes)
     */
    public RecipeProvider(Context _context) {
        mContext = _context;
    }
    //endregion constructors

    //region public overriden methods
    @Override
    public void onCreate(SQLiteDatabase _db) {
        mDatabase = _db;
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mMatcher.addURI(InstalistProvider.AUTHORITY, MULTIPLE_RECIPE_STRING, MULTIPLE_RECIPES);
        mMatcher.addURI(InstalistProvider.AUTHORITY, SINGLE_RECIPE_STRING, SINGLE_RECIPE);
        mMatcher.addURI(InstalistProvider.AUTHORITY, MULTIPLE_RECIPE_INGREDIENT_STRING, MULTIPLE_RECIPE_INGREDIENT);
        mMatcher.addURI(InstalistProvider.AUTHORITY, SINGLE_RECIPE_INGREDIENT_STRING, SINGLE_RECIPE_INGREDIENT);

    }

    @Override
    public Cursor query(@NonNull Uri _uri, String[] _projection, String _selection, String[] _selectionArgs, String _sortOrder) {
        Cursor cursor = null;
        switch (mMatcher.match(_uri)) {

            case SINGLE_RECIPE:
                String selection = ProviderUtils.prependIdToQuery(Recipe.COLUMN.ID, _selection);
                String[] selectionArgs = ProviderUtils.prependSelectionArgs(_selectionArgs, _uri.getLastPathSegment());
                cursor = mDatabase.query(Recipe.TABLE_NAME, _projection, selection, selectionArgs, null, null, _sortOrder);
                break;
            case MULTIPLE_RECIPES:
                cursor = mDatabase.query(Recipe.TABLE_NAME, _projection, _selection, _selectionArgs, null, null, _sortOrder);
                break;
            case SINGLE_RECIPE_INGREDIENT:
                selection = ProviderUtils.prependIdToQuery(Ingredient.COLUMN.ID, _selection);
                selection = ProviderUtils.prependIdToQuery(Ingredient.COLUMN.RECIPE_ID, selection);

                selectionArgs = ProviderUtils.prependSelectionArgs(_selectionArgs, _uri.getLastPathSegment());
                selectionArgs = ProviderUtils.prependSelectionArgs(selectionArgs, _uri.getPathSegments().get(1));

                cursor = mDatabase.query(Ingredient.TABLE_NAME, _projection, selection, selectionArgs, null, null, _sortOrder);
                break;
            case MULTIPLE_RECIPE_INGREDIENT:
                selection = ProviderUtils.prependIdToQuery(Ingredient.COLUMN.RECIPE_ID, _selection);
                selectionArgs = ProviderUtils.prependSelectionArgs(_selectionArgs, _uri.getPathSegments().get(1));
                cursor = mDatabase.query(Ingredient.TABLE_NAME, _projection, selection, selectionArgs, null, null, _sortOrder);
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
            case SINGLE_RECIPE:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + RECIPE_BASE_TYPE;
            case MULTIPLE_RECIPES:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + RECIPE_BASE_TYPE;
            case SINGLE_RECIPE_INGREDIENT:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + INGREDIENT_BASE_TYPE;
            case MULTIPLE_RECIPE_INGREDIENT:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + INGREDIENT_BASE_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(@NonNull Uri _uri, ContentValues _values) {
        Uri newUri = null;
        switch (mMatcher.match(_uri)) {
            case SINGLE_RECIPE:
                long rowId = mDatabase.insert(Recipe.TABLE_NAME, null, _values);
                // insertion went wrong
                if (rowId == -1) {
                    return null;
                }

                newUri = Uri.parse(SINGLE_RECIPE_CONTENT_URI.replace("*",
                        _values.getAsString(Recipe.COLUMN.ID)));
                break;
            case SINGLE_RECIPE_INGREDIENT:
                rowId = mDatabase.insert(Ingredient.TABLE_NAME, null, _values);
                // insertion went wrong
                if (rowId == -1) {
                    return null;
                }
                Cursor cursor = mDatabase.query(Ingredient.TABLE_NAME, Ingredient.COLUMN.ALL_COLUMNS,
                        SQLiteUtils.COLUMN_ROW_ID + " = ?", new String[]{String.valueOf(rowId)},
                        null, null, null, null);
                cursor.moveToFirst();
                String contentUri = SINGLE_RECIPE_INGREDIENT_CONTENT_URI.replaceFirst("\\*", cursor.getString(cursor.getColumnIndex(Ingredient.COLUMN.RECIPE_ID)));
                contentUri = contentUri.replaceFirst("\\*", cursor.getString(cursor.getColumnIndex(Ingredient.COLUMN.ID)));
                newUri = Uri.parse(contentUri);
                cursor.close();
                break;
            case MULTIPLE_RECIPES:
                String newUUID = SQLiteUtils.generateId(mDatabase,Recipe.TABLE_NAME).toString();
                _values.put(Recipe.COLUMN.ID, newUUID);
                rowId = mDatabase.insert(Recipe.TABLE_NAME, null, _values);
                // insertion went wrong
                if (rowId == -1) {
                    return null;
                }

                newUri = Uri.parse(SINGLE_RECIPE_CONTENT_URI.replace("*",
                        newUUID));
                break;
            case MULTIPLE_RECIPE_INGREDIENT:
                newUUID = SQLiteUtils.generateId(mDatabase, Ingredient.TABLE_NAME).toString();
                _values.put(Ingredient.COLUMN.ID, newUUID);
                
                rowId = mDatabase.insert(Ingredient.TABLE_NAME, null, _values);

                // insertion went wrong
                if (rowId == -1) {
                    return null;
                }

                contentUri = SINGLE_RECIPE_INGREDIENT_CONTENT_URI.replaceFirst("\\*", _values.getAsString(Ingredient.COLUMN.RECIPE_ID));
                contentUri = contentUri.replaceFirst("\\*", newUUID);
                newUri = Uri.parse(contentUri);
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
            case SINGLE_RECIPE:
                String selection = ProviderUtils.prependIdToQuery(Recipe.COLUMN_PREFIXED.ID, null);
                String[] selectionArgs = ProviderUtils.prependSelectionArgs(null, _uri.getLastPathSegment());
                affectedRows = mDatabase.delete(Recipe.TABLE_NAME, selection, selectionArgs);
                break;
            case MULTIPLE_RECIPES:
                affectedRows = mDatabase.delete(Recipe.TABLE_NAME, _selection, _selectionArgs);
                break;
            case SINGLE_RECIPE_INGREDIENT:
                selection = ProviderUtils.prependIdToQuery(Ingredient.COLUMN.ID, _selection);
                selection = ProviderUtils.prependIdToQuery(Ingredient.COLUMN.RECIPE_ID, selection);

                selectionArgs = ProviderUtils.prependSelectionArgs(_selectionArgs, _uri.getLastPathSegment());
                selectionArgs = ProviderUtils.prependSelectionArgs(selectionArgs, _uri.getPathSegments().get(1));

                affectedRows = mDatabase.delete(Ingredient.TABLE_NAME, selection, selectionArgs);
                break;
            case MULTIPLE_RECIPE_INGREDIENT:
                selection = ProviderUtils.prependIdToQuery(Ingredient.COLUMN.RECIPE_ID, _selection);
                selectionArgs = ProviderUtils.prependSelectionArgs(_selectionArgs, _uri.getPathSegments().get(1));
                affectedRows = mDatabase.delete(Ingredient.TABLE_NAME, selection, selectionArgs);
                // in this case all recipe ingredients
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
            case SINGLE_RECIPE:
                String selection = ProviderUtils.prependIdToQuery(Recipe.COLUMN.ID, null);
                String[] selectionArgs = ProviderUtils.prependSelectionArgs(null, _uri.getLastPathSegment());
                affectedRows = mDatabase.update(Recipe.TABLE_NAME, _values, selection, selectionArgs);
                break;

            case SINGLE_RECIPE_INGREDIENT:
                selection = ProviderUtils.prependIdToQuery(Ingredient.COLUMN.ID, _selection);
                selection = ProviderUtils.prependIdToQuery(Ingredient.COLUMN.RECIPE_ID, selection);

                selectionArgs = ProviderUtils.prependSelectionArgs(_selectionArgs, _uri.getLastPathSegment());
                selectionArgs = ProviderUtils.prependSelectionArgs(selectionArgs, _uri.getPathSegments().get(1));

                _values.put(Ingredient.COLUMN.ID, _uri.getLastPathSegment());
                _values.put(Ingredient.COLUMN.RECIPE_ID, _uri.getPathSegments().get(1));

                affectedRows = mDatabase.update(Ingredient.TABLE_NAME, _values, selection, selectionArgs);
                break;
            case MULTIPLE_RECIPES:
                // affectedRows = mDatabase.update(Recipe.TABLE_NAME, _values, _selection, _selectionArgs);
                //affectedRows = mDatabase.update(Recipe.TABLE_NAME, _values, _selection, _selectionArgs);
                //break;
            case MULTIPLE_RECIPE_INGREDIENT:
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
