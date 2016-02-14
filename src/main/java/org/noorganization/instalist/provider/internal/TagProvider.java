package org.noorganization.instalist.provider.internal;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.noorganization.instalist.model.Tag;
import org.noorganization.instalist.provider.InstalistProvider;
import org.noorganization.instalist.utils.SQLiteUtils;
import org.noorganization.instalist.view.utils.ProviderUtils;

/**
 * Provider to handle all tag related database actions. It supports actions for single
 * ({@link TagProvider#SINGLE_TAG_CONTENT_URI} and multiple
 * ({@link TagProvider#MULTIPLE_TAG_CONTENT_URI}) tags.
 * Created by Tino on 26.10.2015.
 */
public class TagProvider implements IInternalProvider {

    //region private attributes
    private SQLiteDatabase mDatabase;
    private UriMatcher mMatcher;
    private Context mContext;


    private static final int SINGLE_TAG = 1;
    private static final int MULTIPLE_TAGS = 2;

    private static final String SINGLE_TAG_STRING = "tag/*";
    private static final String MULTIPLE_TAG_STRING = "tag";

    //endregion private attributes

    //region public attributes

    /**
     * The content uri for actions for a single unit.
     */
    public static final String SINGLE_TAG_CONTENT_URI = InstalistProvider.BASE_CONTENT_URI + "/" + SINGLE_TAG_STRING;
    /**
     * The content uri for actions with multiple unit.
     * Supports not {@link android.content.ContentProvider#update(Uri, ContentValues, String, String[])}
     * IMPORTANT:  {@link android.content.ContentProvider#insert(Uri, ContentValues)} will insert an single tag with a generated uuid.
     */
    public static final String MULTIPLE_TAG_CONTENT_URI = InstalistProvider.BASE_CONTENT_URI + "/" + MULTIPLE_TAG_STRING;

    /**
     * The basic subtype for the {@link UnitProvider#getType(Uri)}.
     */
    public static final String TAG_BASE_TYPE = InstalistProvider.BASE_VENDOR + "tag";
    //endregion public attributes

    //region constructors

    /**
     * Constructor of {@link UnitProvider}
     *
     * @param _context the context of the parent provider. (Needed to notify listener for changes)
     */
    public TagProvider(Context _context) {
        mContext = _context;
    }
    //endregion constructors

    //region public overriden methods
    @Override
    public void onCreate(SQLiteDatabase _db) {
        mDatabase = _db;
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        mMatcher.addURI(InstalistProvider.AUTHORITY, MULTIPLE_TAG_STRING, MULTIPLE_TAGS);
        mMatcher.addURI(InstalistProvider.AUTHORITY, SINGLE_TAG_STRING, SINGLE_TAG);

    }

    @Override
    public Cursor query(@NonNull Uri _uri, String[] _projection, String _selection, String[] _selectionArgs, String _sortOrder) {
        Cursor cursor = null;
        switch (mMatcher.match(_uri)) {

            case SINGLE_TAG:
                String selection = ProviderUtils.prependIdToQuery(Tag.COLUMN.ID, _selection);
                String[] selectionArgs = ProviderUtils.prependSelectionArgs(_selectionArgs, _uri.getLastPathSegment());
                cursor = mDatabase.query(Tag.TABLE_NAME, _projection, selection, selectionArgs, null, null, _sortOrder);
                break;
            case MULTIPLE_TAGS:
                cursor = mDatabase.query(Tag.TABLE_NAME, _projection, _selection, _selectionArgs, null, null, _sortOrder);
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
            case SINGLE_TAG:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + TAG_BASE_TYPE;
            case MULTIPLE_TAGS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + TAG_BASE_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(@NonNull Uri _uri, ContentValues _values) {
        Uri newUri = null;
        switch (mMatcher.match(_uri)) {
            case SINGLE_TAG:
                long rowId = mDatabase.insert(Tag.TABLE_NAME, null, _values);
                // insertion went wrong
                if (rowId == -1) {
                    return null;
                    //throw new SQLiteException("Failed to add a record into " + _uri);
                }
                newUri = Uri.parse(SINGLE_TAG_CONTENT_URI.replace("*",
                        _values.getAsString((Tag.COLUMN.ID))));
                break;
            case MULTIPLE_TAGS:
                // insert only single tag with no given id
                _values.put(Tag.COLUMN.ID, SQLiteUtils.generateId(mDatabase, Tag.TABLE_NAME).toString());
                rowId = mDatabase.insert(Tag.TABLE_NAME, null, _values);
                // insertion went wrong
                if (rowId == -1) {
                    return null;
                    //throw new SQLiteException("Failed to add a record into " + _uri);
                }
                newUri = Uri.parse(SINGLE_TAG_CONTENT_URI.replace("*",
                        _values.getAsString((Tag.COLUMN.ID))));
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
            case SINGLE_TAG:
                String selection = ProviderUtils.prependIdToQuery(Tag.COLUMN.ID, null);
                String[] selectionArgs = ProviderUtils.prependSelectionArgs(null, _uri.getLastPathSegment());
                affectedRows = mDatabase.delete(Tag.TABLE_NAME, selection, selectionArgs);
                break;
            case MULTIPLE_TAGS:
                affectedRows = mDatabase.delete(Tag.TABLE_NAME, _selection, _selectionArgs);
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
            case SINGLE_TAG:
                String selection = ProviderUtils.prependIdToQuery(Tag.COLUMN.ID, null);
                String[] selectionArgs = ProviderUtils.prependSelectionArgs(null, _uri.getLastPathSegment());
                affectedRows = mDatabase.update(Tag.TABLE_NAME, _values, selection, selectionArgs);
                break;
            case MULTIPLE_TAGS:
                // TODO for later purposes maybe
                // affectedRows = mDatabase.update(Tag.TABLE_NAME, _values, _selection, _selectionArgs);
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
