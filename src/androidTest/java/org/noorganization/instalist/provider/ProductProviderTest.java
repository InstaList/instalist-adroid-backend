package org.noorganization.instalist.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.provider.internal.IInternalProvider;
import org.noorganization.instalist.provider.internal.ProductProvider;

import java.util.UUID;

/**
 * Test for the {@link ProductProvider} provider.
 * Created by Tino on 24.10.2015.
 */
public class ProductProviderTest extends AndroidTestCase {

    IInternalProvider mProductProvider;
    SQLiteDatabase mDatabase;

    @Override
    public void setUp() {
        mDatabase = new DBOpenHelper(getContext(), null).getWritableDatabase();
        mProductProvider = new ProductProvider(getContext());
        mProductProvider.onCreate(mDatabase);
    }

    @Override
    public void tearDown() {
        resetDb();
        mDatabase.close();
    }

    private void resetDb() {
        mDatabase.delete(Product.TABLE_NAME, null, null);
    }

    public void testQueryMultipleProducts() {
        Uri multipleProductsUri = Uri.parse(ProductProvider.MULTIPLE_PRODUCT_CONTENT_URI);
        Cursor noProduts = mProductProvider.query(multipleProductsUri, null, null, null, null);
        assertNotNull(noProduts);
        assertEquals(0, noProduts.getCount());

        String uuid = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO " + Product.TABLE_NAME + " VALUES (?,?,?,?,?)", new String[]{
                uuid,
                "TestProduct1",
                null,
                "0.5",
                null
        });

        Cursor productCursor = mProductProvider.query(multipleProductsUri, null, null, null, null);
        assertNotNull(productCursor);
        assertEquals(1, productCursor.getCount());
        productCursor.moveToFirst();
        assertEquals("TestProduct1", productCursor.getString(productCursor.getColumnIndex(Product.PREFIXED_COLUMN.NAME)));
        assertEquals(uuid, productCursor.getString(productCursor.getColumnIndex(Product.PREFIXED_COLUMN.ID)));

        String uuid2 = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO " + Product.TABLE_NAME + " VALUES (?,?,?,?,?)", new String[]{
                uuid2,
                "TestProduct2",
                null,
                "0.5",
                null
        });

        productCursor = mProductProvider.query(multipleProductsUri, null, null, null, Product.PREFIXED_COLUMN.NAME + " ASC");
        assertNotNull(productCursor);
        assertEquals(2, productCursor.getCount());
        productCursor.moveToFirst();
        assertEquals("TestProduct1", productCursor.getString(productCursor.getColumnIndex(Product.PREFIXED_COLUMN.NAME)));
        assertEquals(uuid, productCursor.getString(productCursor.getColumnIndex(Product.PREFIXED_COLUMN.ID)));
        productCursor.moveToNext();
        assertEquals("TestProduct2", productCursor.getString(productCursor.getColumnIndex(Product.PREFIXED_COLUMN.NAME)));
        assertEquals(uuid2, productCursor.getString(productCursor.getColumnIndex(Product.PREFIXED_COLUMN.ID)));
        resetDb();
    }

    public void testQuerySingleProduct() {
        Cursor noCategory = mProductProvider.query(Uri.parse(ProductProvider.SINGLE_PRODUCT_CONTENT_URI.replace("*",
                UUID.randomUUID().toString())), null, null, null, null);

        assertNotNull(noCategory);
        assertEquals(0, noCategory.getCount());

        String uuid = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO " + Product.TABLE_NAME + " VALUES (?,?,?,?,?)", new String[]{
                uuid,
                "TestProduct1",
                null,
                "0.5",
                null
        });

        Cursor productCursor = mProductProvider.query(Uri.parse(ProductProvider.SINGLE_PRODUCT_CONTENT_URI.replace("*",
                uuid)), null, null, null, null);
        assertNotNull(productCursor);
        assertEquals(1, productCursor.getCount());
        productCursor.moveToFirst();
        assertEquals("TestProduct1", productCursor.getString(productCursor.getColumnIndex(Product.PREFIXED_COLUMN.NAME)));
        assertEquals(uuid, productCursor.getString(productCursor.getColumnIndex(Product.PREFIXED_COLUMN.ID)));

        String uuid2 = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO " + Product.TABLE_NAME + " VALUES (?,?,?,?,?)", new String[]{
                uuid2,
                "TestProduct2",
                null,
                "0.5",
                null
        });

        productCursor = mProductProvider.query(Uri.parse(ProductProvider.SINGLE_PRODUCT_CONTENT_URI.replace("*", uuid2)),
                null, null, null, null);
        assertNotNull(productCursor);
        assertEquals(1, productCursor.getCount());
        productCursor.moveToFirst();
        assertEquals("TestProduct2", productCursor.getString(productCursor.getColumnIndex(Product.PREFIXED_COLUMN.NAME)));
        assertEquals(uuid2, productCursor.getString(productCursor.getColumnIndex(Product.PREFIXED_COLUMN.ID)));
        resetDb();
    }

    public void testGetTypeSingleProduct() {
        String type = mProductProvider.getType(Uri.parse(ProductProvider.SINGLE_PRODUCT_CONTENT_URI.replace("*", "0")));
        assertEquals(type, ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + ProductProvider.PRODUCT_BASE_TYPE);
    }

    public void testGetTypeMultipleProducts() {
        String type = mProductProvider.getType(Uri.parse(ProductProvider.MULTIPLE_PRODUCT_CONTENT_URI));
        assertEquals(type, ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + ProductProvider.PRODUCT_BASE_TYPE);
    }

    public void testInsertSingleProduct() {
        String uuid;

        Uri uri = ProviderTestUtils.insertProduct(mProductProvider, "TestProduct", 0.5f, 0.5f, (String) null);
        assertNotNull(uri);

        uuid = uri.getLastPathSegment();

        String pseudoUri = ProductProvider.SINGLE_PRODUCT_CONTENT_URI.replace("*", uuid);

        assertEquals(pseudoUri, uri.toString());

        Cursor cursor = mProductProvider.query(uri, Product.PREFIXED_COLUMN.ALL_COLUMNS, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals(uuid, cursor.getString(cursor.getColumnIndex(Product.PREFIXED_COLUMN.ID)));
        assertEquals("TestProduct", cursor.getString(cursor.getColumnIndex(Product.PREFIXED_COLUMN.NAME)));
        assertEquals(0.5f, cursor.getFloat(cursor.getColumnIndex(Product.PREFIXED_COLUMN.STEP_AMOUNT)), 0.001f);
        assertEquals(0.5f, cursor.getFloat(cursor.getColumnIndex(Product.PREFIXED_COLUMN.DEFAULT_AMOUNT)), 0.001f);
        assertEquals(null, cursor.getString(cursor.getColumnIndex(Product.PREFIXED_COLUMN.UNIT)));
    }


    public void testDeleteSingleProduct() {

        Uri uri = ProviderTestUtils.insertProduct(mProductProvider, "TestProduct", 0.5f, 0.5f, (String) null);
        assertNotNull(uri);
        int affectedRows = mProductProvider.delete(uri, null, null);
        assertEquals(1, affectedRows);
        Cursor cursor = mProductProvider.query(uri, Product.PREFIXED_COLUMN.ALL_COLUMNS, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
    }

    public void testDeleteMultipleProducts() {
        Uri uri1 = ProviderTestUtils.insertProduct(mProductProvider, "TestProduct1", 0.5f, 0.5f, (String) null);
        Uri uri2 = ProviderTestUtils.insertProduct(mProductProvider, "TestProduct2", 0.5f, 0.5f, (String) null);

        assertNotNull(uri1);
        assertNotNull(uri2);


        int affectedRows = mProductProvider.delete(Uri.parse(ProductProvider.MULTIPLE_PRODUCT_CONTENT_URI), Product.PREFIXED_COLUMN.NAME + " LIKE ?", new String[]{"%TestProduct%"});

        assertEquals(2, affectedRows);

        uri1 = ProviderTestUtils.insertProduct(mProductProvider, "TestProduct1", 0.5f, 0.5f, (String) null);
        uri2 = ProviderTestUtils.insertProduct(mProductProvider, "TestProduct2", 0.5f, 0.5f, (String) null);

        assertNotNull(uri1);
        assertNotNull(uri2);

        affectedRows = mProductProvider.delete(Uri.parse(ProductProvider.MULTIPLE_PRODUCT_CONTENT_URI), Product.PREFIXED_COLUMN.NAME + " LIKE ?", new String[]{"%TestProduct1%"});
        assertEquals(1, affectedRows);
    }

    public void testUpdateSingleProduct() {
        ContentValues contentValues = new ContentValues();
        String uuid = UUID.randomUUID().toString();

        contentValues.put(Product.COLUMN.ID, uuid);
        contentValues.put(Product.COLUMN.NAME, "TestProduct1");
        contentValues.put(Product.COLUMN.DEFAULT_AMOUNT, 0.5f);
        contentValues.put(Product.COLUMN.STEP_AMOUNT, 0.5f);
        contentValues.put(Product.COLUMN.UNIT, (String) null);

        Uri uri = mProductProvider.insert(Uri.parse(ProductProvider.SINGLE_PRODUCT_CONTENT_URI.replace("*", uuid)), contentValues);

        contentValues.put(Product.COLUMN.DEFAULT_AMOUNT, 1.0f);
        int affectedRows = mProductProvider.update(uri, contentValues, null, null);

        assertEquals(1, affectedRows);

        Cursor cursor = mProductProvider.query(uri, Product.PREFIXED_COLUMN.ALL_COLUMNS, null, null, null);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();

        assertEquals(1.0f, cursor.getFloat(cursor.getColumnIndex(Product.PREFIXED_COLUMN.DEFAULT_AMOUNT)), 0.001f);
    }

}
