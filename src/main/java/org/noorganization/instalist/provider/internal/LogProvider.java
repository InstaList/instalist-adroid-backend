package org.noorganization.instalist.provider.internal;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import org.noorganization.instalist.provider.InstalistProvider;

/**
 * Provider that offers access to logs and the times when the last time there was a change.
 * Created by Desnoo on 16.02.2016.
 */
public class LogProvider implements IInternalProvider {
    private static final String TAG = "LogProvider";

    /**
     * Currently not used, but already assigned for consistent use and reserved for future.
     */
    private Context mContext;
    private SQLiteDatabase mDatabase;
    private UriMatcher mMatcher;

    private static final int LOG_DIRECTORY = 1;


    public LogProvider(Context _context) {
        mContext = _context;
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        mMatcher.addURI(InstalistProvider.AUTHORITY, "log", LOG_DIRECTORY);
    }

    @Override
    public void onCreate(SQLiteDatabase _db) {
        mDatabase = _db;
    }

    @Override
    public Cursor query(@NonNull Uri _uri, String[] _projection, String _selection, String[] _selectionArgs, String _sortOrder) {
        switch (mMatcher.match(_uri)) {
            case LOG_DIRECTORY:
                return mDatabase.query(org.noorganization.instalist.model.Log.TABLE_NAME, _projection, _selection, _selectionArgs, null, null, _sortOrder);
            default:
                return null;
        }
    }

    @Override
    public String getType(@NonNull Uri _uri) {

        switch (mMatcher.match(_uri)) {
            case LOG_DIRECTORY:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + InstalistProvider.BASE_VENDOR +
                        "log";
            default:
                return null;
        }
    }

    @Override
    public Uri insert(@NonNull Uri _uri, ContentValues _values) {
        Log.e(TAG, "insert: Operation is not supported.");
        throw new UnsupportedOperationException("This operation is not supported, the insertion is only triggered through triggers.");
    }

    @Override
    public int delete(@NonNull Uri _uri, String _selection, String[] _selectionArgs) {
        switch (mMatcher.match(_uri)) {
            case LOG_DIRECTORY:
                return mDatabase.delete(org.noorganization.instalist.model.Log.TABLE_NAME, _selection, _selectionArgs);
            default:
                return 0;
        }
    }

    @Override
    public int update(@NonNull Uri _uri, ContentValues _values, String _selection, String[] _selectionArgs) {
        Log.e(TAG, "insert: Operation is not supported.");
        throw new UnsupportedOperationException("This operation is not supported, the insertion is only triggered through triggers.");
    }
}
