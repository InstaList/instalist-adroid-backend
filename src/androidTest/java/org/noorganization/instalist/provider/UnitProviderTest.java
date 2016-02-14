package org.noorganization.instalist.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

import org.noorganization.instalist.model.Unit;
import org.noorganization.instalist.provider.internal.IInternalProvider;
import org.noorganization.instalist.provider.internal.UnitProvider;

import java.util.UUID;

/**
 * Test for the {@link UnitProvider} provider.
 * Created by Tino on 24.10.2015.
 */
public class UnitProviderTest extends AndroidTestCase {

    IInternalProvider mUnitProvider;
    SQLiteDatabase mDatabase;

    @Override
    public void setUp() {
        mDatabase = new DBOpenHelper(getContext(), null).getWritableDatabase();
        mUnitProvider = new UnitProvider(getContext());
        mUnitProvider.onCreate(mDatabase);
    }

    @Override
    public void tearDown() {
        resetDb();
        mDatabase.close();
    }

    private void resetDb() {
        mDatabase.delete(Unit.TABLE_NAME, null, null);
    }

    public void testQueryMultipleUnits() {
        Uri multipleUnitsUri = Uri.parse(UnitProvider.MULTIPLE_UNIT_CONTENT_URI);
        Cursor noProduts = mUnitProvider.query(multipleUnitsUri, null, null, null, null);
        assertNotNull(noProduts);
        assertEquals(0, noProduts.getCount());

        String uuid = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO " + Unit.TABLE_NAME + " VALUES (?,?)", new String[]{
                uuid,
                "TestUnit1"
        });

        Cursor productCursor = mUnitProvider.query(multipleUnitsUri, null, null, null, null);
        assertNotNull(productCursor);
        assertEquals(1, productCursor.getCount());
        productCursor.moveToFirst();
        assertEquals("TestUnit1", productCursor.getString(productCursor.getColumnIndex(Unit.COLUMN.NAME)));
        assertEquals(uuid, productCursor.getString(productCursor.getColumnIndex(Unit.COLUMN.ID)));

        String uuid2 = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO " + Unit.TABLE_NAME + " VALUES (?,?)", new String[]{
                uuid2,
                "TestUnit2"
        });

        productCursor = mUnitProvider.query(multipleUnitsUri, null, null, null, Unit.COLUMN.NAME + " ASC");
        assertNotNull(productCursor);
        assertEquals(2, productCursor.getCount());
        productCursor.moveToFirst();
        assertEquals("TestUnit1", productCursor.getString(productCursor.getColumnIndex(Unit.COLUMN.NAME)));
        assertEquals(uuid, productCursor.getString(productCursor.getColumnIndex(Unit.COLUMN.ID)));
        productCursor.moveToNext();
        assertEquals("TestUnit2", productCursor.getString(productCursor.getColumnIndex(Unit.COLUMN.NAME)));
        assertEquals(uuid2, productCursor.getString(productCursor.getColumnIndex(Unit.COLUMN.ID)));
        resetDb();
    }

    public void testQuerySingleUnit() {
        Cursor noCategory = mUnitProvider.query(Uri.parse(UnitProvider.SINGLE_UNIT_CONTENT_URI.replace("*",
                UUID.randomUUID().toString())), null, null, null, null);

        assertNotNull(noCategory);
        assertEquals(0, noCategory.getCount());

        String uuid = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO " + Unit.TABLE_NAME + " VALUES (?,?)", new String[]{
                uuid,
                "TestUnit1"
        });

        Cursor productCursor = mUnitProvider.query(Uri.parse(UnitProvider.SINGLE_UNIT_CONTENT_URI.replace("*",
                uuid)), null, null, null, null);
        assertNotNull(productCursor);
        assertEquals(1, productCursor.getCount());
        productCursor.moveToFirst();
        assertEquals("TestUnit1", productCursor.getString(productCursor.getColumnIndex(Unit.COLUMN.NAME)));
        assertEquals(uuid, productCursor.getString(productCursor.getColumnIndex(Unit.COLUMN.ID)));

        String uuid2 = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO " + Unit.TABLE_NAME + " VALUES (?,?)", new String[]{
                uuid2,
                "TestUnit2"
        });

        productCursor = mUnitProvider.query(Uri.parse(UnitProvider.SINGLE_UNIT_CONTENT_URI.replace("*", uuid2)),
                null, null, null, null);
        assertNotNull(productCursor);
        assertEquals(1, productCursor.getCount());
        productCursor.moveToFirst();
        assertEquals("TestUnit2", productCursor.getString(productCursor.getColumnIndex(Unit.COLUMN.NAME)));
        assertEquals(uuid2, productCursor.getString(productCursor.getColumnIndex(Unit.COLUMN.ID)));
        resetDb();
    }

    public void testGetTypeSingleUnit() {
        String type = mUnitProvider.getType(Uri.parse(UnitProvider.SINGLE_UNIT_CONTENT_URI.replace("*", "0")));
        assertEquals(type, ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + UnitProvider.UNIT_BASE_TYPE);
    }

    public void testGetTypeMultipleUnits() {
        String type = mUnitProvider.getType(Uri.parse(UnitProvider.MULTIPLE_UNIT_CONTENT_URI));
        assertEquals(type, ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + UnitProvider.UNIT_BASE_TYPE);
    }

    public void testInsertSingleUnit() {
        ContentValues contentValues = new ContentValues();
        String uuid = UUID.randomUUID().toString();

        contentValues.put(Unit.COLUMN.ID, uuid);
        contentValues.put(Unit.COLUMN.NAME, "TestUnit");

        Uri uri = mUnitProvider.insert(Uri.parse(UnitProvider.SINGLE_UNIT_CONTENT_URI.replace("*", uuid)), contentValues);
        String pseudoUri = UnitProvider.SINGLE_UNIT_CONTENT_URI.replace("*", uuid);
        assertNotNull(uri);
        assertEquals(pseudoUri, uri.toString());

        Cursor cursor = mUnitProvider.query(uri, Unit.PREFIXED_COLUMN.ALL_COLUMNS, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals(uuid, cursor.getString(cursor.getColumnIndex(Unit.COLUMN.ID)));
        assertEquals("TestUnit", cursor.getString(cursor.getColumnIndex(Unit.COLUMN.NAME)));
        resetDb();
    }


    public void testDeleteSingleUnit() {

        ContentValues contentValues = new ContentValues();
        String uuid = UUID.randomUUID().toString();

        contentValues.put(Unit.COLUMN.ID, uuid);
        contentValues.put(Unit.COLUMN.NAME, "TestUnit");


        Uri uri = mUnitProvider.insert(Uri.parse(UnitProvider.SINGLE_UNIT_CONTENT_URI.replace("*", uuid)), contentValues);

        int affectedRows = mUnitProvider.delete(uri, null, null);
        assertEquals(1, affectedRows);
        Cursor cursor = mUnitProvider.query(uri, Unit.PREFIXED_COLUMN.ALL_COLUMNS, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
    }

    public void testDeleteMultipleUnits() {
        ContentValues contentValues = new ContentValues();
        String uuid = UUID.randomUUID().toString();

        contentValues.put(Unit.COLUMN.ID, uuid);
        contentValues.put(Unit.COLUMN.NAME, "TestUnit1");

        mUnitProvider.insert(Uri.parse(UnitProvider.SINGLE_UNIT_CONTENT_URI.replace("*", uuid)), contentValues);

        String uuid2 = UUID.randomUUID().toString();

        contentValues.put(Unit.COLUMN.ID, uuid2);
        contentValues.put(Unit.COLUMN.NAME, "TestUnit2");

        mUnitProvider.insert(Uri.parse(UnitProvider.SINGLE_UNIT_CONTENT_URI.replace("*", uuid2)), contentValues);

        int affectedRows = mUnitProvider.delete(Uri.parse(UnitProvider.MULTIPLE_UNIT_CONTENT_URI), Unit.COLUMN.NAME + " LIKE ?", new String[]{"%TestUnit%"});

        assertEquals(2, affectedRows);


        contentValues.put(Unit.COLUMN.ID, uuid);
        contentValues.put(Unit.COLUMN.NAME, "TestUnit1");

        mUnitProvider.insert(Uri.parse(UnitProvider.SINGLE_UNIT_CONTENT_URI.replace("*", uuid)), contentValues);

        contentValues.put(Unit.COLUMN.ID, uuid2);
        contentValues.put(Unit.COLUMN.NAME, "TestUnit2");

        mUnitProvider.insert(Uri.parse(UnitProvider.SINGLE_UNIT_CONTENT_URI.replace("*", uuid2)), contentValues);

        affectedRows = mUnitProvider.delete(Uri.parse(UnitProvider.MULTIPLE_UNIT_CONTENT_URI), Unit.COLUMN.NAME + " LIKE ?", new String[]{"%TestUnit1%"});

        assertEquals(1, affectedRows);
    }

    public void testUpdateSingleUnit() {
        ContentValues contentValues = new ContentValues();
        String uuid = UUID.randomUUID().toString();

        contentValues.put(Unit.COLUMN.ID, uuid);
        contentValues.put(Unit.COLUMN.NAME, "TestUnit1");

        Uri uri = mUnitProvider.insert(Uri.parse(UnitProvider.SINGLE_UNIT_CONTENT_URI.replace("*", uuid)), contentValues);

        contentValues.put(Unit.COLUMN.NAME, "TestUnit2");
        int affectedRows = mUnitProvider.update(uri, contentValues, null, null);

        assertEquals(1, affectedRows);

        Cursor cursor = mUnitProvider.query(uri, Unit.PREFIXED_COLUMN.ALL_COLUMNS, null, null, null);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();

        assertEquals("TestUnit2", cursor.getString(cursor.getColumnIndex(Unit.COLUMN.NAME)));
    }

    public void testUpdateMultipleUnits() {
        // TODO: implement
    }
}
