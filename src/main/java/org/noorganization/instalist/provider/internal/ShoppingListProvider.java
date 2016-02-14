package org.noorganization.instalist.provider.internal;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.provider.InstalistProvider;
import org.noorganization.instalist.utils.SQLiteUtils;

/**
 * Readonly provider for searching for lists. Insert, update and delete do simply nothing.
 * Created by damihe on 11.11.15.
 */
public class ShoppingListProvider implements IInternalProvider {

    /**
     * Currently not used, but already assigned for consistent use and reserved for future.
     */
    private Context        mContext;
    private SQLiteDatabase mDatabase;
    private UriMatcher     mMatcher;

    private static final int LIST_DIRECTORY = 1;
    private static final int LIST_ITEM = 2;

    public ShoppingListProvider(Context _context) {
        mContext = _context;
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        mMatcher.addURI(InstalistProvider.AUTHORITY, "list", LIST_DIRECTORY);
        mMatcher.addURI(InstalistProvider.AUTHORITY, "list/*", LIST_ITEM);
    }

    @Override
    public void onCreate(SQLiteDatabase _db) {
        mDatabase = _db;
    }

    @Override
    public Cursor query(@NonNull Uri _uri, String[] _projection, String _selection,
                        String[] _selectionArgs, String _sortOrder) {
        switch (mMatcher.match(_uri)) {
            case LIST_DIRECTORY: {
                return mDatabase.query(ShoppingList.TABLE_NAME, _projection, _selection,
                        _selectionArgs, null, null, _sortOrder);
            }
            case LIST_ITEM: {
                String selection = SQLiteUtils.prependSelection(ShoppingList.COLUMN.ID + " = ?",
                        _selection);
                String[] args = SQLiteUtils.prependSelectionArgs(_uri.getLastPathSegment(),
                        _selectionArgs);
                return mDatabase.query(ShoppingList.TABLE_NAME, _projection, selection, args, null,
                        null, _sortOrder);
            }
            default:
                return null;
        }
    }

    @Override
    public String getType(@NonNull Uri _uri) {
        switch (mMatcher.match(_uri)) {
            case LIST_DIRECTORY:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + InstalistProvider.BASE_VENDOR +
                        "list";
            case LIST_ITEM:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + InstalistProvider.BASE_VENDOR +
                        "list";
            default:
                return null;
        }
    }

    @Override
    public Uri insert(@NonNull Uri _uri, ContentValues _values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri _uri, String _selection, String[] _selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri _uri, ContentValues _values, String _selection, String[] _selectionArgs) {
        return 0;
    }
}
