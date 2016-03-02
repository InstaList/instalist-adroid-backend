/*
 * Copyright 2016 Tino Siegmund, Michael Wodniok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noorganization.instalist.provider.internal;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.noorganization.instalist.model.ListEntry;
import org.noorganization.instalist.provider.InstalistProvider;
import org.noorganization.instalist.utils.SQLiteUtils;

/**
 * Read-only provider for list-entries.
 * Created by damihe on 11.11.15.
 */
public class ListEntryProvider implements IInternalProvider {

    /**
     * Currently not used, but already assigned for consistent use and reserved for future.
     */
    private Context mContext;
    private SQLiteDatabase mDatabase;
    private UriMatcher mMatcher;

    private static final int ENTRY_DIRECTORY = 1;
    private static final int ENTRY_ITEM = 2;

    public ListEntryProvider(Context _context) {
        mContext = _context;
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        mMatcher.addURI(InstalistProvider.AUTHORITY, "entry", ENTRY_DIRECTORY);
        mMatcher.addURI(InstalistProvider.AUTHORITY, "entry/*", ENTRY_ITEM);
    }

    @Override
    public void onCreate(SQLiteDatabase _db) {
        mDatabase = _db;
    }

    @Override
    public Cursor query(@NonNull Uri _uri, String[] _projection, String _selection,
                        String[] _selectionArgs, String _sortOrder) {
        switch (mMatcher.match(_uri)) {
            case ENTRY_DIRECTORY: {
                return mDatabase.query(ListEntry.TABLE_NAME, _projection, _selection,
                        _selectionArgs, null, null, _sortOrder);
            }
            case ENTRY_ITEM: {
                String selection = SQLiteUtils.prependSelection(ListEntry.COLUMN.ID + " = ?",
                        _selection);
                String[] args = SQLiteUtils.prependSelectionArgs(_uri.getLastPathSegment(),
                        _selectionArgs);
                return mDatabase.query(ListEntry.TABLE_NAME, _projection, selection, args, null,
                        null, _sortOrder);
            }
            default:
                return null;
        }
    }

    @Override
    public String getType(@NonNull Uri _uri) {
        switch (mMatcher.match(_uri)) {
            case ENTRY_DIRECTORY:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + InstalistProvider.BASE_VENDOR +
                        "entry";
            case ENTRY_ITEM:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + InstalistProvider.BASE_VENDOR +
                        "entry";
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