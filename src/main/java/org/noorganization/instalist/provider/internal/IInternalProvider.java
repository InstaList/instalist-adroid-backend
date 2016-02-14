package org.noorganization.instalist.provider.internal;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.noorganization.instalist.provider.InstalistProvider;

/**
 * This is a "proxy" for splitting the provider into several parts. The semantic of methods is the
 * same as of {@link android.content.ContentProvider}. The interface is thought only for
 * implementations in {@link org.noorganization.instalist.provider.internal} and use by
 * {@link InstalistProvider}.
 * Created by damihe on 21.10.15.
 */
public interface IInternalProvider {

    /**
     * onCreate will be called on startup. For details, look at {@link ContentProvider#onCreate()}.
     * @param _db The Database created/opened through the needed driver. Use this database for all
     *            operations.
     */
    void onCreate(SQLiteDatabase _db);

    /**
     * query will be called when a reading request should be handled. For details, look at
     * {@link ContentProvider#query(Uri, String[], String, String[], String)}. The queries got
     * already filtered by {@link InstalistProvider} (if request was asked) so that the first
     * path segment is always the right.
     */
    Cursor query(@NonNull Uri _uri, String[] _projection, String _selection, String[] _selectionArgs, String _sortOrder);

    /**
     * getType will be called when a client wants to know the type returned by a query.For details,
     * look at {@link ContentProvider#getType(Uri)}. The Uri's got already filtered by
     * {@link InstalistProvider} (if request was asked through) so that the first path segment is
     * always the right.
     */
    String getType(@NonNull Uri _uri);

    /**
     * insert will be called when a client wants to add some data. For details, look at
     * {@link ContentProvider#insert(Uri, ContentValues)}. The Uri's got already filtered by
     * {@link InstalistProvider} (if request was asked through) so that the first path segment is
     * always the right.
     */
    Uri insert(@NonNull Uri _uri, ContentValues _values);

    /**
     * delete will be called when a client wants to remove some data. For details, look at
     * {@link ContentProvider#delete(Uri, String, String[])}. The Uri's got already filtered by
     * {@link InstalistProvider} (if request was asked through) so that the first path segment is
     * always the right.
     * @return the number of affected rows.
     */
    int delete(@NonNull Uri _uri, String _selection, String[] _selectionArgs);

    /**
     * update will be called when a client wants to change data. For details, look at
     * {@link ContentProvider#update(Uri, ContentValues, String, String[])}. The Uri's got already
     * filtered by {@link InstalistProvider} (if request was asked through) so that the first path
     * segment is always the right.
     * @return the number of affected rows.
     */
    int update(@NonNull Uri _uri, ContentValues _values, String _selection, String[] _selectionArgs);


}
