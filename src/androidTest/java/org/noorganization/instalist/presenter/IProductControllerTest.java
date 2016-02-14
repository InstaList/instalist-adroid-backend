package org.noorganization.instalist.presenter;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import org.noorganization.instalist.presenter.implementation.ControllerFactory;
import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.model.ListEntry;
import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.model.Tag;
import org.noorganization.instalist.model.TaggedProduct;
import org.noorganization.instalist.model.Unit;
import org.noorganization.instalist.provider.InstalistProvider;
import org.noorganization.instalist.provider.ProviderTestUtils;
import org.noorganization.instalist.provider.internal.ProductProvider;
import org.noorganization.instalist.provider.internal.TagProvider;
import org.noorganization.instalist.provider.internal.TaggedProductProvider;
import org.noorganization.instalist.provider.internal.UnitProvider;

public class IProductControllerTest extends AndroidTestCase {

    Unit mLiter;
    Product mMilk;
    Product mBroccoli;
    ShoppingList mList;
    Tag mTag;

    IProductController mController2Test;
    IUnitController mUnitController;
    ITagController mTagController;
    IListController mListController;

    ContentResolver mResolver;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mResolver = mContext.getContentResolver();
        mUnitController = ControllerFactory.getUnitController(mContext);
        mController2Test = ControllerFactory.getProductController(mContext);
        mTagController = ControllerFactory.getTagController(mContext);
        mListController = ControllerFactory.getListController(mContext);

        tearDown();

        Uri unitUri = ProviderTestUtils.insertUnit(mResolver, "_TEST_l");
        assertNotNull(unitUri);
        mLiter = new Unit(unitUri.getLastPathSegment(), "_TEST_1");//ProviderTestUtils.getUnit(mResolver, unitUri);

        Uri productUri = ProviderTestUtils.insertProduct(mResolver, "_TEST_milk", 1.0f, 1.0f, mLiter.mUUID);
        assertNotNull(productUri);
        mMilk = new Product(productUri.getLastPathSegment(), "_TEST_milk", mLiter, 1.0f, 1.0f); // ProviderTestUtils.getProduct(mResolver, productUri.getLastPathSegment());

        productUri = ProviderTestUtils.insertProduct(mResolver, "_TEST_broccoli", 1.0f, 1.0f, null);
        assertNotNull(productUri);
        mBroccoli = new Product(productUri.getLastPathSegment(), "_TEST_broccoli", null, 1.0f, 1.0f); //ProviderTestUtils.getProduct(mResolver, productUri.getLastPathSegment());

        Uri tagUri = ProviderTestUtils.insertTag(mResolver, "_TEST_vegetable");
        assertNotNull(tagUri);
        mTag = new Tag(tagUri.getLastPathSegment(), "_TEST_vegetable"); //ProviderTestUtils.getTag(mResolver, tagUri);

        Uri categoryUri = ProviderTestUtils.insertCategory(mResolver, "TEST_CATEGORY");
        assertNotNull(categoryUri);
        Category testCategory = new Category(categoryUri.getLastPathSegment(), "TEST_CATEGORY");
        Uri listUri = ProviderTestUtils.insertList(mResolver, categoryUri.getLastPathSegment(), "TEST_LIST");
        assertNotNull(listUri);

        mList = new ShoppingList(listUri.getLastPathSegment(), "TEST_LIST", testCategory);

        Uri newListEntryUri = ProviderTestUtils.insertListEntry(mResolver, mList.mCategory.mUUID, mList.mUUID, mMilk.mUUID, 1.0f, false, 0);
        assertNotNull(newListEntryUri);
//        assertNotNull(mController2Test.addTagToProduct(mBroccoli, mTag));

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        mResolver.delete(Uri.parse(TaggedProductProvider.MULTIPLE_TAGGED_PRODUCT_CONTENT_URI), null, null);

        Cursor entryCursor = mResolver.query(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "entry"), ListEntry.COLUMN.ALL_COLUMNS, null, null, null);
        Cursor listCursor = mResolver.query(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "list"), ShoppingList.COLUMN.ALL_COLUMNS, null, null, null);
        Cursor categoryCursor = mResolver.query(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category"), Category.COLUMN.ALL_COLUMNS, null, null, null);


        if (categoryCursor == null) {
            throw new IllegalStateException("No category Cursor found.");
        }
        if (listCursor == null) {
            throw new IllegalStateException("No ShoppingList Cursor found.");
        }
        if (entryCursor == null) {
            throw new IllegalStateException("No ListEntry cursor found.");
        }

        if (entryCursor.getCount() > 0) {
            entryCursor.moveToFirst();
            do {
                String listEntryId = entryCursor.getString(entryCursor.getColumnIndex(ListEntry.COLUMN.ID));
                mResolver.delete(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "entry/" + listEntryId), null, null);
            } while (entryCursor.moveToNext());
        }
        if (listCursor.getCount() > 0) {
            listCursor.moveToFirst();
            do {
                String shoppingListId = listCursor.getString(listCursor.getColumnIndex(ShoppingList.COLUMN.ID));
                mResolver.delete(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "list/" + shoppingListId), null, null);
            } while (listCursor.moveToNext());
        }
        if (categoryCursor.getCount() > 0) {
            categoryCursor.moveToFirst();
            do {
                String categoryId = categoryCursor.getString(categoryCursor.getColumnIndex(Category.COLUMN.ID));
                ProviderTestUtils.deleteTestLists(mResolver, categoryId);
                int deleted = mResolver.delete(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category/" + categoryId), null, null);
                if(deleted == 0){
                    Log.e("IProductControllerTest", "ID: " + categoryCursor.getString(categoryCursor.getColumnIndex(Category.COLUMN.ID)) + " " + categoryCursor.getString(categoryCursor.getColumnIndex(Category.COLUMN.NAME)) );
                }
                assertEquals(1, deleted);
            } while (categoryCursor.moveToNext());
        }
        categoryCursor.close();
        listCursor.close();
        entryCursor.close();

        mResolver.delete(Uri.parse(ProductProvider.MULTIPLE_PRODUCT_CONTENT_URI), null, null);
        mResolver.delete(Uri.parse(UnitProvider.MULTIPLE_UNIT_CONTENT_URI), null, null);
        mResolver.delete(Uri.parse(TagProvider.MULTIPLE_TAG_CONTENT_URI), null, null);
    }

    public void testCreateProduct() throws Exception {
        // Should not work because milk already exists.
        assertNull(mController2Test.createProduct("_TEST_milk", mLiter, 1.0f, 1.0f));
        // Should not work because a parameter is not ok.
        assertNull(mController2Test.createProduct("_TEST_butter", mLiter, 0.0f, 1.0f));

        Product createdProduct = mController2Test.createProduct("_TEST_butter", null, 1.0f, 0.25f);
        assertNotNull(createdProduct);

        assertEquals("_TEST_butter", createdProduct.mName);
        assertEquals(0.25f, createdProduct.mStepAmount, 0.001f);
        assertEquals(1.0f, createdProduct.mDefaultAmount, 0.001);
        assertEquals("-", createdProduct.mUnit.mUUID);

        Cursor productsCursor = mResolver.query(Uri.parse(ProductProvider.MULTIPLE_PRODUCT_CONTENT_URI), Product.COLUMN.ALL_COLUMNS, Product.COLUMN.ID + "=?", new String[]{createdProduct.mUUID}, null);
        assertNotNull(productsCursor);
        assertEquals(1, productsCursor.getCount());
        productsCursor.moveToFirst();

        assertEquals("_TEST_butter", productsCursor.getString(productsCursor.getColumnIndex(Product.COLUMN.NAME)));
        assertEquals(0.25f, productsCursor.getFloat(productsCursor.getColumnIndex(Product.COLUMN.STEP_AMOUNT)), 0.001f);
        assertEquals(1.0f, productsCursor.getFloat(productsCursor.getColumnIndex(Product.COLUMN.DEFAULT_AMOUNT)), 0.001);
        assertEquals("-", productsCursor.getString(productsCursor.getColumnIndex(Product.COLUMN.UNIT)));

        productsCursor.close();
    }

    public void testModifyProduct() throws Exception {
        mMilk.mDefaultAmount = -200.0f;
        Product returnedProduct = mController2Test.modifyProduct(mMilk);
        assertFalse(mMilk.equals(returnedProduct));
        assertNotNull(returnedProduct);
        assertEquals(1.0f, returnedProduct.mDefaultAmount, 0.001f);

        mMilk.mDefaultAmount = 200.0f;
        returnedProduct = mController2Test.modifyProduct(mMilk);
        assertEquals(mMilk, returnedProduct);
    }

    public void testRemoveProduct() throws Exception {
        assertFalse(mController2Test.removeProduct(mMilk, false));
        assertNotNull(mController2Test.findById(mMilk.mUUID));

        Cursor listEntryCursor = mResolver.query(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "entry"),
                ListEntry.COLUMN.ALL_COLUMNS,
                ListEntry.COLUMN.PRODUCT + "=?",
                new String[]{mMilk.mUUID},
                null
        );
        assertNotNull(listEntryCursor);
        assertEquals(1, listEntryCursor.getCount());
        listEntryCursor.close();

        assertTrue(mController2Test.removeProduct(mBroccoli, false));
        assertNull(mController2Test.findById(mBroccoli.mUUID));

        Cursor taggedProductCursor = mResolver.query(Uri.parse(TaggedProductProvider.MULTIPLE_TAGGED_PRODUCT_CONTENT_URI),
                TaggedProduct.ALL_COLUMNS_JOINED,
                TaggedProduct.COLUMN_PREFIXED.PRODUCT_ID + "=?",
                new String[]{mBroccoli.mUUID},
                null
        );
        assertNotNull(taggedProductCursor);

        assertEquals(0, taggedProductCursor.getCount());
        taggedProductCursor.close();

        assertTrue(mController2Test.removeProduct(mMilk, true));
        assertNull(mController2Test.findById(mMilk.mUUID));

        listEntryCursor = mResolver.query(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "entry"),
                ListEntry.COLUMN.ALL_COLUMNS,
                ListEntry.COLUMN.PRODUCT + "=?",
                new String[]{mMilk.mUUID},
                null
        );
        assertNotNull(listEntryCursor);
        assertEquals(0, listEntryCursor.getCount());
        listEntryCursor.close();

    }

    public void testAddTagToProduct() throws Exception {
        assertNotNull(mController2Test.addTagToProduct(mMilk, mTag));

        Cursor taggedProductCursor = mResolver.query(Uri.parse(TaggedProductProvider.MULTIPLE_TAGGED_PRODUCT_CONTENT_URI),
                TaggedProduct.ALL_COLUMNS_JOINED,
                TaggedProduct.COLUMN_PREFIXED.PRODUCT_ID + "=? AND " + TaggedProduct.COLUMN_PREFIXED.TAG_ID + "=?",
                new String[]{mMilk.mUUID, mTag.mUUID},
                null
        );
        assertNotNull(taggedProductCursor);
        assertEquals(0, taggedProductCursor.getCount());
    }

    public void testRemoveTagFromProduct() throws Exception {
        mController2Test.removeTagFromProduct(mBroccoli, mTag);
        Cursor taggedProductCursor = mResolver.query(Uri.parse(TaggedProductProvider.MULTIPLE_TAGGED_PRODUCT_CONTENT_URI),
                TaggedProduct.ALL_COLUMNS_JOINED,
                TaggedProduct.COLUMN_PREFIXED.PRODUCT_ID + "=? AND " + TaggedProduct.COLUMN_PREFIXED.TAG_ID + "=?",
                new String[]{mBroccoli.mUUID, mTag.mUUID},
                null
        );
        assertNotNull(taggedProductCursor);
        assertEquals(0, taggedProductCursor.getCount());
    }
}
