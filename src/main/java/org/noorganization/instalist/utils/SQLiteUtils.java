package org.noorganization.instalist.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import java.util.Map;
import java.util.UUID;

/**
 * Additional utils and helper functions for sqlite.
 * Created by Tino on 24.10.2015.
 */
public class SQLiteUtils {

    /**
     * The sqlite rowid column.
     */
    public final static String COLUMN_ROW_ID = "rowid";

    /**
     * Prepends a selection-string using a SQL-AND-conjunction.
     * @param _prependedSelection The additional selection.
     * @param _originalSelection The original selection string. May be null (that means, there was
     *                           no original selection).
     * @return A combined selection string.
     */
    public static String prependSelection(@NonNull String _prependedSelection, String _originalSelection) {
        if (_originalSelection != null) {
            return "(" + _prependedSelection + ") AND (" + _originalSelection + ")";
        } else {
            return _prependedSelection;
        }
    }

    /**
     * Alias for {@link #prependSelectionArgs(String[], String[])}. But for only one additional
     * argument for better readable code.
     * @see #prependSelectionArgs(String[], String[])
     */
    public static String[] prependSelectionArgs(@NonNull String _prependedSelectionArg,
                                                String[] _originalSelectionArgs) {
        return prependSelectionArgs(new String[]{ _prependedSelectionArg }, _originalSelectionArgs);
    }

    /**
     * Prepends selection args.
     * @param _prependedSelectionArgs The new selection-arguments to prepend.
     * @param _originalSelectionArgs The previous selection-arguments. May be null.
     * @return The combined array of selection arguments. If _originalSelectionArgs are null,
     * _prependedSelectionArgs get returned.
     */
    public static String[] prependSelectionArgs(@NonNull String[] _prependedSelectionArgs,
                                                String[] _originalSelectionArgs) {
        if (_originalSelectionArgs == null || _originalSelectionArgs.length == 0) {
            return _prependedSelectionArgs;
        }

        int newArrayLength = _prependedSelectionArgs.length + _originalSelectionArgs.length;
        String[] rtn = new String[newArrayLength];
        System.arraycopy(_prependedSelectionArgs, 0, rtn, 0, _prependedSelectionArgs.length);
        System.arraycopy(_originalSelectionArgs, 0, rtn, _prependedSelectionArgs.length,
                _originalSelectionArgs.length);
        return rtn;
    }

    /**
     * Generate a column map for usage with
     * {@link android.database.sqlite.SQLiteQueryBuilder#setProjectionMap(Map)}.
     * @param _table The table to generate the map for. The table is not checked for existence but
     *               must not be empty.
     * @param _column As many columns as needed. May not be empty.
     * @return A Map containing all given columns projection-maps.
     */
    public static Map<String, String> generateProjectionMap(@NonNull String _table, String... _column) {
        Map<String, String> rtn = new ArrayMap<>(_column.length);
        for (String currentColumn : _column) {
            rtn.put(currentColumn, _table + "." + currentColumn);
        }
        return rtn;
    }

    public static UUID generateId(SQLiteDatabase _db, String _table) {
        UUID rtn = null;
        while (rtn == null) {
            rtn = UUID.randomUUID();
            Cursor counter = _db.query(_table, new String[]{ "_id" },
                    "_id = ?", new String[]{ rtn.toString() }, null, null, null);
            if (counter.getCount() != 0) {
                rtn = null;
            }
            counter.close();
        }
        return rtn;
    }
}
