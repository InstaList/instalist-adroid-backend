package org.noorganization.instalist.provider.internal;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.noorganization.instalist.model.Unit;
import org.noorganization.instalist.provider.InstalistProvider;
import org.noorganization.instalist.utils.SQLiteUtils;
import org.noorganization.instalist.view.utils.ProviderUtils;

/**
 * Provider to handle provider actions for Units. Supports actions for single
 * ({@link UnitProvider#SINGLE_UNIT_CONTENT_URI}) and multiple units
 * ({@link UnitProvider#MULTIPLE_UNIT_CONTENT_URI}).
 * Created by Tino on 26.10.2015.
 */
public class UnitProvider implements IInternalProvider {
    //region private attributes
    private SQLiteDatabase mDatabase;
    private UriMatcher mMatcher;
    private Context mContext;

    private static final int SINGLE_UNIT = 1;
    private static final int MULTIPLE_UNITS = 2;

    private static final String SINGLE_UNIT_STRING = "unit/*";
    private static final String MULTIPLE_UNIT_STRING = "unit";

    //endregion private attributes

    //region public attributes

    /**
     * The content uri for actions for a single unit.
     */
    public static final String SINGLE_UNIT_CONTENT_URI = InstalistProvider.BASE_CONTENT_URI + "/" + SINGLE_UNIT_STRING;

    /**
     * The content uri for actions with multiple units.
     * Does not support {@link android.content.ContentProvider#insert(Uri, ContentValues)},
     * {@link android.content.ContentProvider#update(Uri, ContentValues, String, String[])} ,
     */
    public static final String MULTIPLE_UNIT_CONTENT_URI = InstalistProvider.BASE_CONTENT_URI + "/" + MULTIPLE_UNIT_STRING;

    /**
     * The basic subtype for the {@link UnitProvider#getType(Uri)}.
     */
    public static final String UNIT_BASE_TYPE = InstalistProvider.BASE_VENDOR + "unit";
    //endregion public attributes

    //region constructors

    /**
     * Constructor of {@link UnitProvider}
     *
     * @param _context the context of the parent provider. (Needed to notify listener for changes)
     */
    public UnitProvider(Context _context) {
        mContext = _context;
    }
    //endregion constructors

    //region public overriden methods
    @Override
    public void onCreate(SQLiteDatabase _db) {
        mDatabase = _db;
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        mMatcher.addURI(InstalistProvider.AUTHORITY, MULTIPLE_UNIT_STRING, MULTIPLE_UNITS);
        mMatcher.addURI(InstalistProvider.AUTHORITY, SINGLE_UNIT_STRING, SINGLE_UNIT);
    }

    @Override
    public Cursor query(@NonNull Uri _uri, String[] _projection, String _selection, String[] _selectionArgs, String _sortOrder) {
        Cursor cursor = null;
        switch (mMatcher.match(_uri)) {

            case SINGLE_UNIT:
                String selection = ProviderUtils.prependIdToQuery(Unit.COLUMN.ID, _selection);
                String[] selectionArgs = ProviderUtils.prependSelectionArgs(_selectionArgs, _uri.getLastPathSegment());
                cursor = mDatabase.query(Unit.TABLE_NAME, _projection, selection, selectionArgs, null, null, _sortOrder);
                break;
            case MULTIPLE_UNITS:
                cursor = mDatabase.query(Unit.TABLE_NAME, _projection, _selection, _selectionArgs, null, null, _sortOrder);
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
            case SINGLE_UNIT:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + UNIT_BASE_TYPE;
            case MULTIPLE_UNITS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + UNIT_BASE_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(@NonNull Uri _uri, ContentValues _values) {
        Uri newUri = null;
        switch (mMatcher.match(_uri)) {
            case SINGLE_UNIT:
                long rowId = mDatabase.insert(Unit.TABLE_NAME, null, _values);
                // insertion went wrong
                if (rowId == -1) {
                    return null;
                    //throw new SQLiteException("Failed to add a record into " + _uri);
                }

                newUri = Uri.parse(SINGLE_UNIT_CONTENT_URI.replace("*", _values.getAsString(Unit.COLUMN.ID)));
                break;
            case MULTIPLE_UNITS:
                _values.put(Unit.COLUMN.ID, SQLiteUtils.generateId(mDatabase, Unit.TABLE_NAME).toString());
                rowId = mDatabase.insert(Unit.TABLE_NAME, null, _values);
                // insertion went wrong
                if (rowId == -1) {
                    return null;
                    //throw new SQLiteException("Failed to add a record into " + _uri);
                }

                newUri = Uri.parse(SINGLE_UNIT_CONTENT_URI.replace("*", _values.getAsString(Unit.COLUMN.ID)));
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
            case SINGLE_UNIT:
                String selection = ProviderUtils.prependIdToQuery(Unit.COLUMN.ID, null);
                String[] selectionArgs = ProviderUtils.prependSelectionArgs(null, _uri.getLastPathSegment());
                affectedRows = mDatabase.delete(Unit.TABLE_NAME, selection, selectionArgs);
                break;
            case MULTIPLE_UNITS:
                affectedRows = mDatabase.delete(Unit.TABLE_NAME, _selection, _selectionArgs);
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
            case SINGLE_UNIT:
                String selection = ProviderUtils.prependIdToQuery(Unit.COLUMN.ID, null);
                String[] selectionArgs = ProviderUtils.prependSelectionArgs(null, _uri.getLastPathSegment());
                affectedRows = mDatabase.update(Unit.TABLE_NAME, _values, selection, selectionArgs);
                break;
            case MULTIPLE_UNITS:
                // TODO for later purposes maybe
                // affectedRows = mDatabase.update(Unit.TABLE_NAME, _values, _selection, _selectionArgs);
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
