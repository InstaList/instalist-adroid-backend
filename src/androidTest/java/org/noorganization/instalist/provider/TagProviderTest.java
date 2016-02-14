package org.noorganization.instalist.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

import org.noorganization.instalist.model.Tag;
import org.noorganization.instalist.provider.internal.IInternalProvider;
import org.noorganization.instalist.provider.internal.TagProvider;

import java.util.UUID;

/**
 * Test for the {@link TagProvider} provider.
 * Created by Tino on 24.10.2015.
 */
public class TagProviderTest extends AndroidTestCase {

    IInternalProvider mTagProvider;
    SQLiteDatabase mDatabase;

    @Override
    public void setUp() {
        mDatabase = new DBOpenHelper(getContext(), null).getWritableDatabase();
        mTagProvider = new TagProvider(getContext());
        mTagProvider.onCreate(mDatabase);
    }

    @Override
    public void tearDown() {
        resetDb();
        mDatabase.close();
    }

    private void resetDb() {
        mDatabase.delete(Tag.TABLE_NAME, null, null);
    }

    public void testQueryMultipleTags() {
        Uri multipleTagsUri = Uri.parse(TagProvider.MULTIPLE_TAG_CONTENT_URI);
        Cursor noProduts = mTagProvider.query(multipleTagsUri, null, null, null, null);
        assertNotNull(noProduts);
        assertEquals(0, noProduts.getCount());

        String uuid = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO " + Tag.TABLE_NAME + " VALUES (?,?)", new String[]{
                uuid,
                "TestTag1"
        });

        Cursor productCursor = mTagProvider.query(multipleTagsUri, null, null, null, null);
        assertNotNull(productCursor);
        assertEquals(1, productCursor.getCount());
        productCursor.moveToFirst();
        assertEquals("TestTag1", productCursor.getString(productCursor.getColumnIndex(Tag.COLUMN_PREFIXED.NAME)));
        assertEquals(uuid, productCursor.getString(productCursor.getColumnIndex(Tag.COLUMN_PREFIXED.ID)));

        String uuid2 = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO " + Tag.TABLE_NAME + " VALUES (?,?)", new String[]{
                uuid2,
                "TestTag2"
        });

        productCursor = mTagProvider.query(multipleTagsUri, null, null, null, Tag.COLUMN_PREFIXED.NAME + " ASC");
        assertNotNull(productCursor);
        assertEquals(2, productCursor.getCount());
        productCursor.moveToFirst();
        assertEquals("TestTag1", productCursor.getString(productCursor.getColumnIndex(Tag.COLUMN_PREFIXED.NAME)));
        assertEquals(uuid, productCursor.getString(productCursor.getColumnIndex(Tag.COLUMN_PREFIXED.ID)));
        productCursor.moveToNext();
        assertEquals("TestTag2", productCursor.getString(productCursor.getColumnIndex(Tag.COLUMN_PREFIXED.NAME)));
        assertEquals(uuid2, productCursor.getString(productCursor.getColumnIndex(Tag.COLUMN_PREFIXED.ID)));
        resetDb();
    }

    public void testQuerySingleTag() {
        Cursor noCategory = mTagProvider.query(Uri.parse(TagProvider.SINGLE_TAG_CONTENT_URI.replace("*",
                UUID.randomUUID().toString())), null, null, null, null);

        assertNotNull(noCategory);
        assertEquals(0, noCategory.getCount());

        String uuid = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO " + Tag.TABLE_NAME + " VALUES (?,?)", new String[]{
                uuid,
                "TestTag1"
        });

        Cursor productCursor = mTagProvider.query(Uri.parse(TagProvider.SINGLE_TAG_CONTENT_URI.replace("*",
                uuid)), null, null, null, null);
        assertNotNull(productCursor);
        assertEquals(1, productCursor.getCount());
        productCursor.moveToFirst();
        assertEquals("TestTag1", productCursor.getString(productCursor.getColumnIndex(Tag.COLUMN_PREFIXED.NAME)));
        assertEquals(uuid, productCursor.getString(productCursor.getColumnIndex(Tag.COLUMN_PREFIXED.ID)));

        String uuid2 = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO " + Tag.TABLE_NAME + " VALUES (?,?)", new String[]{
                uuid2,
                "TestTag2"
        });

        productCursor = mTagProvider.query(Uri.parse(TagProvider.SINGLE_TAG_CONTENT_URI.replace("*", uuid2)),
                null, null, null, null);
        assertNotNull(productCursor);
        assertEquals(1, productCursor.getCount());
        productCursor.moveToFirst();
        assertEquals("TestTag2", productCursor.getString(productCursor.getColumnIndex(Tag.COLUMN_PREFIXED.NAME)));
        assertEquals(uuid2, productCursor.getString(productCursor.getColumnIndex(Tag.COLUMN_PREFIXED.ID)));
        resetDb();
    }

    public void testGetTypeSingleTag() {
        String type = mTagProvider.getType(Uri.parse(TagProvider.SINGLE_TAG_CONTENT_URI.replace("*", "0")));
        assertEquals(type, ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + TagProvider.TAG_BASE_TYPE);
    }

    public void testGetTypeMultipleTags() {
        String type = mTagProvider.getType(Uri.parse(TagProvider.MULTIPLE_TAG_CONTENT_URI));
        assertEquals(type, ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + TagProvider.TAG_BASE_TYPE);
    }

    public void testInsertSingleTag() {
        ContentValues contentValues = new ContentValues();
        String uuid = UUID.randomUUID().toString();

        contentValues.put(Tag.COLUMN.ID, uuid);
        contentValues.put(Tag.COLUMN.NAME, "TestTag");

        Uri uri = mTagProvider.insert(Uri.parse(TagProvider.SINGLE_TAG_CONTENT_URI.replace("*", uuid)), contentValues);
        String pseudoUri = TagProvider.SINGLE_TAG_CONTENT_URI.replace("*", uuid);
        assertNotNull(uri);
        assertEquals(pseudoUri, uri.toString());

        Cursor cursor = mTagProvider.query(uri, Tag.COLUMN_PREFIXED.ALL_COLUMNS, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals(uuid, cursor.getString(cursor.getColumnIndex(Tag.COLUMN_PREFIXED.ID)));
        assertEquals("TestTag", cursor.getString(cursor.getColumnIndex(Tag.COLUMN_PREFIXED.NAME)));
        resetDb();
    }


    public void testDeleteSingleTag() {

        ContentValues contentValues = new ContentValues();
        String uuid = UUID.randomUUID().toString();

        contentValues.put(Tag.COLUMN.ID, uuid);
        contentValues.put(Tag.COLUMN.NAME, "TestTag");


        Uri uri = mTagProvider.insert(Uri.parse(TagProvider.SINGLE_TAG_CONTENT_URI.replace("*", uuid)), contentValues);

        int affectedRows = mTagProvider.delete(uri, null, null);
        assertEquals(1, affectedRows);
        Cursor cursor = mTagProvider.query(uri, Tag.COLUMN_PREFIXED.ALL_COLUMNS, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
    }

    public void testDeleteMultipleTags() {
        ContentValues contentValues = new ContentValues();
        String uuid = UUID.randomUUID().toString();

        contentValues.put(Tag.COLUMN.ID, uuid);
        contentValues.put(Tag.COLUMN.NAME, "TestTag1");

        mTagProvider.insert(Uri.parse(TagProvider.SINGLE_TAG_CONTENT_URI.replace("*", uuid)), contentValues);

        String uuid2 = UUID.randomUUID().toString();

        contentValues.put(Tag.COLUMN.ID, uuid2);
        contentValues.put(Tag.COLUMN.NAME, "TestTag2");

        mTagProvider.insert(Uri.parse(TagProvider.SINGLE_TAG_CONTENT_URI.replace("*", uuid2)), contentValues);

        int affectedRows = mTagProvider.delete(Uri.parse(TagProvider.MULTIPLE_TAG_CONTENT_URI), Tag.COLUMN_PREFIXED.NAME + " LIKE ?", new String[]{"%TestTag%"});

        assertEquals(2, affectedRows);


        contentValues.put(Tag.COLUMN.ID, uuid);
        contentValues.put(Tag.COLUMN.NAME, "TestTag1");

        mTagProvider.insert(Uri.parse(TagProvider.SINGLE_TAG_CONTENT_URI.replace("*", uuid)), contentValues);

        contentValues.put(Tag.COLUMN.ID, uuid2);
        contentValues.put(Tag.COLUMN.NAME, "TestTag2");

        mTagProvider.insert(Uri.parse(TagProvider.SINGLE_TAG_CONTENT_URI.replace("*", uuid2)), contentValues);

        affectedRows = mTagProvider.delete(Uri.parse(TagProvider.MULTIPLE_TAG_CONTENT_URI), Tag.COLUMN_PREFIXED.NAME + " LIKE ?", new String[]{"%TestTag1%"});

        assertEquals(1, affectedRows);
    }

    public void testUpdateSingleTag() {
        ContentValues contentValues = new ContentValues();
        String uuid = UUID.randomUUID().toString();

        contentValues.put(Tag.COLUMN.ID, uuid);
        contentValues.put(Tag.COLUMN.NAME, "TestTag1");

        Uri uri = mTagProvider.insert(Uri.parse(TagProvider.SINGLE_TAG_CONTENT_URI.replace("*", uuid)), contentValues);

        contentValues.put(Tag.COLUMN.NAME, "TestTag2");
        int affectedRows = mTagProvider.update(uri, contentValues, null, null);

        assertEquals(1, affectedRows);

        Cursor cursor = mTagProvider.query(uri, Tag.COLUMN_PREFIXED.ALL_COLUMNS, null, null, null);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();

        assertEquals("TestTag2", cursor.getString(cursor.getColumnIndex(Tag.COLUMN_PREFIXED.NAME)));
    }

}
