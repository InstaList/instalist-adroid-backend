package org.noorganization.instalist.provider.internal;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.model.ListEntry;
import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.provider.InstalistProvider;
import org.noorganization.instalist.utils.SQLiteUtils;

/**
 * TODO: implement and describe.
 * Created by damihe on 21.10.15.
 */
public class CategoryProvider implements IInternalProvider {

    private SQLiteDatabase mDatabase;
    private UriMatcher mMatcher;

    private static final int CATEGORY_DIRECTORY = 1;
    private static final int CATEGORY_ITEM = 2;
    private static final int LIST_DIRECTORY = 3;
    private static final int LIST_ITEM = 4;
    private static final int ENTRY_DIRECTORY = 5;
    private static final int ENTRY_ITEM = 6;

    @Override
    public void onCreate(SQLiteDatabase _db) {
        mDatabase = _db;
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mMatcher.addURI(InstalistProvider.AUTHORITY, "category", CATEGORY_DIRECTORY);
        mMatcher.addURI(InstalistProvider.AUTHORITY, "category/*", CATEGORY_ITEM);
        mMatcher.addURI(InstalistProvider.AUTHORITY, "category/*/list", LIST_DIRECTORY);
        mMatcher.addURI(InstalistProvider.AUTHORITY, "category/*/list/*", LIST_ITEM);
        mMatcher.addURI(InstalistProvider.AUTHORITY, "category/*/list/*/entry", ENTRY_DIRECTORY);
        mMatcher.addURI(InstalistProvider.AUTHORITY, "category/*/list/*/entry/*", ENTRY_ITEM);
    }

    @Override
    public Cursor query(@NonNull Uri _uri, String[] _projection, String _selection, String[] _selectionArgs, String _sortOrder) {
        switch (mMatcher.match(_uri)) {
            case CATEGORY_DIRECTORY:
                return mDatabase.query(Category.TABLE_NAME, _projection, _selection,
                        _selectionArgs, null, null, _sortOrder);
            case CATEGORY_ITEM: {
                String selection = SQLiteUtils.prependSelection(Category.COLUMN.ID + " = ?",
                        _selection);
                String[] selectionArgs = SQLiteUtils.prependSelectionArgs(_uri.getLastPathSegment(),
                        _selectionArgs);
                return mDatabase.query(Category.TABLE_NAME, _projection, selection, selectionArgs,
                        null, null, _sortOrder);
            }
            case LIST_DIRECTORY: {
                String category = _uri.getPathSegments().get(1);
                if (category.equals("-")) {
                    String selection = SQLiteUtils.prependSelection(ShoppingList.COLUMN.CATEGORY +
                            " IS NULL", _selection);
                    return mDatabase.query(ShoppingList.TABLE_NAME, _projection, selection,
                            _selectionArgs, null, null, _sortOrder);
                } else {
                    String selection = SQLiteUtils.prependSelection(ShoppingList.COLUMN.CATEGORY +
                            " = ?", _selection);
                    String[] selectionArgs = SQLiteUtils.prependSelectionArgs(category,
                            _selectionArgs);
                    return mDatabase.query(ShoppingList.TABLE_NAME, _projection, selection,
                            selectionArgs, null, null, _sortOrder);
                }
            }
            case LIST_ITEM: {
                String category = _uri.getPathSegments().get(1);
                if (category.equals("-")) {
                    String selection = SQLiteUtils.prependSelection(ShoppingList.COLUMN.CATEGORY +
                            " IS NULL AND " + ShoppingList.COLUMN.ID + " = ?", _selection);
                    String[] selectionArgs = SQLiteUtils.prependSelectionArgs(
                            _uri.getLastPathSegment(),
                            _selectionArgs);
                    return mDatabase.query(ShoppingList.TABLE_NAME, _projection, selection,
                            selectionArgs, null, null, _sortOrder);
                } else {
                    String selection = SQLiteUtils.prependSelection(ShoppingList.COLUMN.CATEGORY +
                                    " = ? AND " + ShoppingList.COLUMN.ID + " = ?",
                            _selection);
                    String[] selectionArgs = SQLiteUtils.prependSelectionArgs(
                            new String[]{category, _uri.getLastPathSegment()},
                            _selectionArgs);
                    return mDatabase.query(ShoppingList.TABLE_NAME, _projection, selection,
                            selectionArgs, null, null, _sortOrder);
                }
            }
            case ENTRY_DIRECTORY: {
                String category = _uri.getPathSegments().get(1);
                String list = _uri.getPathSegments().get(3);
                SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
                queryBuilder.setTables("(" + ListEntry.TABLE_NAME + " INNER JOIN " +
                        ShoppingList.TABLE_NAME + " ON (" + ShoppingList.TABLE_NAME + "." +
                        ShoppingList.COLUMN.ID + " = " + ListEntry.TABLE_NAME + "." +
                        ListEntry.COLUMN.LIST + ")) INNER JOIN " + Product.TABLE_NAME + " ON (" +
                        Product.PREFIXED_COLUMN.ID + " = " + ListEntry.TABLE_NAME
                        + "." + ListEntry.COLUMN.PRODUCT + ")");
                queryBuilder.setProjectionMap(SQLiteUtils.generateProjectionMap(ListEntry.TABLE_NAME,
                        ListEntry.COLUMN.ID, ListEntry.COLUMN.AMOUNT, ListEntry.COLUMN.LIST,
                        ListEntry.COLUMN.PRIORITY, ListEntry.COLUMN.PRODUCT, ListEntry.COLUMN.STRUCK));
                if (category.equals("-")) {
                    String selection = SQLiteUtils.prependSelection(
                            "list.category IS NULL AND " + ListEntry.COLUMN.LIST + " = ?",
                            _selection);
                    String[] args = SQLiteUtils.prependSelectionArgs(list, _selectionArgs);
                    return queryBuilder.query(mDatabase, _projection, selection, args, null, null,
                            _sortOrder);
                } else {
                    String selection = SQLiteUtils.prependSelection("list.category = ? AND " +
                                    ListEntry.COLUMN.LIST + " = ?",
                            _selection);
                    String[] args = SQLiteUtils.prependSelectionArgs(new String[]{category, list},
                            _selectionArgs);
                    return queryBuilder.query(mDatabase, _projection, selection, args, null, null,
                            _sortOrder);
                }
            }
            case ENTRY_ITEM: {
                String category = _uri.getPathSegments().get(1);
                String list = _uri.getPathSegments().get(3);
                String entry = _uri.getLastPathSegment();
                SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
                queryBuilder.setTables("(" + ListEntry.TABLE_NAME + " INNER JOIN " +
                        ShoppingList.TABLE_NAME + " ON (" + ShoppingList.TABLE_NAME + "." +
                        ShoppingList.COLUMN.ID + " = " + ListEntry.TABLE_NAME + "." +
                        ListEntry.COLUMN.LIST + ")) INNER JOIN " + Product.TABLE_NAME + " ON (" +
                        Product.PREFIXED_COLUMN.ID + " = " + ListEntry.TABLE_NAME
                        + "." + ListEntry.COLUMN.PRODUCT + ")");
                queryBuilder.setProjectionMap(SQLiteUtils.generateProjectionMap(ListEntry.TABLE_NAME,
                        ListEntry.COLUMN.ID, ListEntry.COLUMN.AMOUNT, ListEntry.COLUMN.LIST,
                        ListEntry.COLUMN.PRIORITY, ListEntry.COLUMN.PRODUCT, ListEntry.COLUMN.STRUCK));
                if (category.equals("-")) {
                    String selection = SQLiteUtils.prependSelection(ShoppingList.TABLE_NAME + "." +
                                    ShoppingList.COLUMN.CATEGORY + " IS NULL AND " +
                                    ListEntry.TABLE_NAME + "." + ListEntry.COLUMN.LIST +
                                    " = ? AND " + ListEntry.TABLE_NAME + "." +
                                    ListEntry.COLUMN.ID + " = ?",
                            _selection);
                    String[] args = SQLiteUtils.prependSelectionArgs(new String[]{
                            list,
                            entry
                    }, _selectionArgs);
                    return queryBuilder.query(mDatabase, _projection, selection, args, null, null,
                            _sortOrder);
                } else {
                    String selection = SQLiteUtils.prependSelection(ShoppingList.TABLE_NAME + "." +
                                    ShoppingList.COLUMN.CATEGORY + " = ? AND " +
                                    ListEntry.TABLE_NAME + "." + ListEntry.COLUMN.LIST +
                                    " = ? AND " + ListEntry.TABLE_NAME + "." +
                                    ListEntry.COLUMN.ID + " = ?",
                            _selection);
                    String[] args = SQLiteUtils.prependSelectionArgs(new String[]{
                                    category,
                                    list,
                                    entry
                            },
                            _selectionArgs);
                    return queryBuilder.query(mDatabase, _projection, selection, args, null, null,
                            _sortOrder);
                }
            }
            default:
                return null;
        }
    }

    @Override
    public String getType(@NonNull Uri _uri) {
        switch (mMatcher.match(_uri)) {
            case CATEGORY_DIRECTORY:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + InstalistProvider.BASE_VENDOR +
                        "category";
            case CATEGORY_ITEM:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + InstalistProvider.BASE_VENDOR +
                        "category";
            case LIST_DIRECTORY:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + InstalistProvider.BASE_VENDOR +
                        "list";
            case LIST_ITEM:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + InstalistProvider.BASE_VENDOR +
                        "list";
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
        if (_values == null) {
            return null;
        }
        switch (mMatcher.match(_uri)) {
            case CATEGORY_DIRECTORY: {
                String name = _values.getAsString(Category.COLUMN.NAME);
                if (name == null) {
                    return null;
                }
                String newCatUUID = SQLiteUtils.generateId(mDatabase, Category.TABLE_NAME).
                        toString();
                ContentValues toInsert = new ContentValues(2);
                toInsert.put(Category.COLUMN.ID, newCatUUID);
                toInsert.put(Category.COLUMN.NAME, name);
                if (mDatabase.insert(Category.TABLE_NAME, null, toInsert) != -1) {
                    return Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI,
                            "category/" + newCatUUID);
                } else {
                    return null;
                }
            }
            case CATEGORY_ITEM: {
                String uuid = _values.getAsString(Category.COLUMN.ID);
                String name = _values.getAsString(Category.COLUMN.NAME);
                if (uuid == null || name == null) {
                    return null;
                }
                if (mDatabase.insert(Category.TABLE_NAME, null, _values) != -1) {
                    return Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI,
                            "category/" + uuid);
                }
                return null;

            }
            case LIST_DIRECTORY: {
                String name = _values.getAsString(ShoppingList.COLUMN.NAME);
                if (name == null) {
                    return null;
                }
                String categoryId = _uri.getPathSegments().get(1);
                String newListUUID = SQLiteUtils.generateId(mDatabase, ShoppingList.TABLE_NAME).
                        toString();
                ContentValues toInsert = new ContentValues(3);
                toInsert.put(ShoppingList.COLUMN.ID, newListUUID);
                toInsert.put(ShoppingList.COLUMN.NAME, name);
                if (!"-".equals(categoryId)) {
                    toInsert.put(ShoppingList.COLUMN.CATEGORY, categoryId);
                }
                if (mDatabase.insert(ShoppingList.TABLE_NAME, null, toInsert) != -1) {
                    return Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category/" +
                            categoryId + "/list/" + newListUUID);
                } else {
                    return null;
                }
            }
            case ENTRY_DIRECTORY: {
                if (!_values.containsKey(ListEntry.COLUMN.PRODUCT)) {
                    return null;
                }
                ContentValues toInsert = new ContentValues();
                for (String cvKey : _values.keySet()) {
                    switch (cvKey) {
                        case ListEntry.COLUMN.AMOUNT:
                        case ListEntry.COLUMN.PRIORITY:
                            toInsert.put(cvKey, _values.getAsFloat(cvKey));
                            break;
                        case ListEntry.COLUMN.PRODUCT:
                            toInsert.put(cvKey, _values.getAsString(cvKey));
                            break;
                        case ListEntry.COLUMN.STRUCK:
                            Boolean struckValue = _values.getAsBoolean(cvKey);
                            if (struckValue == null) {
                                struckValue = (_values.getAsInteger(cvKey) != 0);
                            }
                            toInsert.put(cvKey, (struckValue ? 1 : 0));
                            break;
                    }
                }

                String listUUID = _uri.getPathSegments().get(3);
                String newEntryUUID = SQLiteUtils.generateId(mDatabase, ListEntry.TABLE_NAME).toString();
                toInsert.put(ListEntry.COLUMN.ID, newEntryUUID);
                toInsert.put(ListEntry.COLUMN.LIST, listUUID);
                if (mDatabase.insert(ListEntry.TABLE_NAME, null, toInsert) != -1) {
                    return Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category/" +
                            _uri.getPathSegments().get(1) + "/list/" + listUUID + "/entry/" +
                            newEntryUUID);
                } else {
                    return null;
                }
            }
            default:
                return null;
        }
    }

    @Override
    public int delete(@NonNull Uri _uri, String _selection, String[] _selectionArgs) {
        switch (mMatcher.match(_uri)) {
            case CATEGORY_ITEM: {
                String categoryId = _uri.getLastPathSegment();
                if ("-".equals(categoryId)) {
                    return 0;
                }
                String selection = SQLiteUtils.prependSelection(Category.COLUMN.ID + " = ?", _selection);
                String[] args = SQLiteUtils.prependSelectionArgs(categoryId, _selectionArgs);
                return mDatabase.delete(Category.TABLE_NAME, selection, args);
            }
            case LIST_ITEM: {
                String categoryUUID = _uri.getPathSegments().get(1);
                String listUUID = _uri.getLastPathSegment();
                String selection;
                String[] args;
                if ("-".equals(categoryUUID)) {
                    selection = SQLiteUtils.prependSelection(ShoppingList.COLUMN.ID + " = ? AND " +
                            ShoppingList.COLUMN.CATEGORY + " IS NULL", _selection);
                    args = SQLiteUtils.prependSelectionArgs(listUUID, _selectionArgs);
                } else {
                    selection = SQLiteUtils.prependSelection(ShoppingList.COLUMN.ID + " = ? AND " +
                            ShoppingList.COLUMN.CATEGORY + " = ?", _selection);
                    args = SQLiteUtils.prependSelectionArgs(new String[]{listUUID, categoryUUID},
                            _selectionArgs);
                }
                return mDatabase.delete(ShoppingList.TABLE_NAME, selection, args);
            }
            case ENTRY_ITEM: {
                String categoryUUID = _uri.getPathSegments().get(1);
                String listUUID = _uri.getPathSegments().get(3);
                String entryUUID = _uri.getLastPathSegment();
                mDatabase.beginTransaction();
                Cursor catCheckCursor = null;
                try {
                    catCheckCursor = mDatabase.query(
                            ShoppingList.TABLE_NAME,
                            new String[]{ShoppingList.COLUMN.CATEGORY},
                            ShoppingList.COLUMN.ID + " = ?",
                            new String[]{listUUID},
                            null, null, null);
                    catCheckCursor.moveToFirst();
                    String currentCat = catCheckCursor.getString(catCheckCursor.
                            getColumnIndex(ShoppingList.COLUMN.CATEGORY));
                    if (("-".equals(categoryUUID) && currentCat == null) ||
                            categoryUUID.equals(currentCat)) {
                        String selection = SQLiteUtils.prependSelection(ListEntry.COLUMN.ID +
                                " = ? AND " + ListEntry.COLUMN.LIST + " = ?", _selection);
                        String[] args = SQLiteUtils.prependSelectionArgs(
                                new String[]{
                                        entryUUID,
                                        listUUID
                                }, _selectionArgs);
                        int changedLines = mDatabase.delete(ListEntry.TABLE_NAME, selection, args);
                        if (changedLines != -1) {
                            mDatabase.setTransactionSuccessful();
                        }
                        return changedLines;
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                } finally {
                    if (catCheckCursor != null) {
                        catCheckCursor.close();
                    }
                    mDatabase.endTransaction();
                }
                return 0;
            }
        }
        return 0;
    }

    @Override
    public int update(@NonNull Uri _uri, ContentValues _values, String _selection, String[] _selectionArgs) {
        switch (mMatcher.match(_uri)) {
            case CATEGORY_ITEM: {
                String categoryUUID = _uri.getLastPathSegment();
                ContentValues toUpdate = new ContentValues(1);
                toUpdate.put(Category.COLUMN.NAME, _values.getAsString(Category.COLUMN.NAME));
                String selection = SQLiteUtils.prependSelection(Category.COLUMN.ID + " = ?",
                        _selection);
                String[] args = SQLiteUtils.prependSelectionArgs(categoryUUID, _selectionArgs);
                return mDatabase.update(Category.TABLE_NAME, toUpdate, selection, args);
            }
            case LIST_ITEM: {
                String categoryUUID = _uri.getPathSegments().get(1);
                ContentValues toUpdate = new ContentValues(2);
                for (String cvKey : _values.keySet()) {
                    switch (cvKey) {
                        case ShoppingList.COLUMN.NAME:
                        case ShoppingList.COLUMN.CATEGORY:
                            toUpdate.put(cvKey, _values.getAsString(cvKey));
                            break;
                    }
                }
                String selection;
                String[] args;
                if ("-".equals(categoryUUID)) {
                    selection = SQLiteUtils.prependSelection(ShoppingList.COLUMN.ID + " = ? AND " +
                            ShoppingList.COLUMN.CATEGORY + " IS NULL", _selection);
                    args = new String[]{_uri.getLastPathSegment()};
                } else {
                    selection = SQLiteUtils.prependSelection(ShoppingList.COLUMN.ID + " = ? AND " +
                            ShoppingList.COLUMN.CATEGORY + " = ?", _selection);
                    args = new String[]{_uri.getLastPathSegment(), categoryUUID};
                }
                return mDatabase.update(ShoppingList.TABLE_NAME, toUpdate, selection, args);
            }
            case ENTRY_ITEM: {
                String categoryUUID = _uri.getPathSegments().get(1);
                String listUUID = _uri.getPathSegments().get(3);
                String entryUUID = _uri.getLastPathSegment();
                ContentValues toUpdate = new ContentValues();
                for (String cvKey : _values.keySet()) {
                    switch (cvKey) {
                        case ListEntry.COLUMN.LIST:
                        case ListEntry.COLUMN.PRODUCT:
                            toUpdate.put(cvKey, _values.getAsString(cvKey));
                            break;
                        case ListEntry.COLUMN.AMOUNT:
                        case ListEntry.COLUMN.PRIORITY:
                            toUpdate.put(cvKey, _values.getAsFloat(cvKey));
                            break;
                        case ListEntry.COLUMN.STRUCK: {
                            Object struckObj = _values.get(cvKey);
                            if (struckObj instanceof Boolean) {
                                toUpdate.put(cvKey, ((Boolean) struckObj ? 1 : 0));
                            } else {
                                toUpdate.put(cvKey, (((Integer) struckObj) != 0 ? 1 : 0));
                            }
                            break;
                        }
                    }
                }
                mDatabase.beginTransaction();
                try {
                    Cursor checkCatCursor = mDatabase.query(
                            ShoppingList.TABLE_NAME,
                            new String[]{ShoppingList.COLUMN.CATEGORY},
                            ShoppingList.COLUMN.ID + " = ?",
                            new String[]{listUUID},
                            null, null, null);
                    if (checkCatCursor.getCount() == 0) {
                        return 0;
                    }
                    checkCatCursor.moveToFirst();
                    String currentCat = checkCatCursor.getString(checkCatCursor.getColumnIndex(
                            ShoppingList.COLUMN.CATEGORY));

                    if (("-".equals(categoryUUID) && currentCat == null) || categoryUUID.
                            equals(currentCat)) {
                        String selection = SQLiteUtils.prependSelection(ListEntry.COLUMN.ID +
                                " = ? AND " + ListEntry.COLUMN.LIST + " = ?", _selection);
                        String[] args = SQLiteUtils.prependSelectionArgs(new String[]{
                                entryUUID,
                                listUUID
                        }, _selectionArgs);
                        int changedRows = mDatabase.update(ListEntry.TABLE_NAME, toUpdate,
                                selection, args);
                        if (changedRows != -1) {
                            mDatabase.setTransactionSuccessful();
                            return changedRows;
                        }
                    }
                } catch (SQLiteException _printed) {
                    _printed.printStackTrace();
                } finally {
                    mDatabase.endTransaction();
                }
                return 0;
            }
            default:
                return 0;
        }
    }
}
