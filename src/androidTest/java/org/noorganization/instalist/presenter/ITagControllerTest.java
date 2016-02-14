package org.noorganization.instalist.presenter;

import android.content.ContentResolver;
import android.net.Uri;
import android.test.AndroidTestCase;

import org.noorganization.instalist.presenter.implementation.ControllerFactory;
import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.Tag;
import org.noorganization.instalist.model.TaggedProduct;
import org.noorganization.instalist.provider.ProviderTestUtils;
import org.noorganization.instalist.provider.internal.ProductProvider;
import org.noorganization.instalist.provider.internal.TagProvider;
import org.noorganization.instalist.provider.internal.TaggedProductProvider;

public class ITagControllerTest extends AndroidTestCase {

    static final String TEST_MILK_PRODUCT = "_TEST_milkProduct";

    Tag mMetalware;
    Tag mMilkProduct;
    Product mCheese;
    TaggedProduct mCheeseMilkProductTag;
    ITagController mTagController;
    IProductController mProductController;
    ContentResolver mResolver;

    public void setUp() throws Exception {
        super.setUp();
        mProductController = ControllerFactory.getProductController(mContext);
        mTagController = ControllerFactory.getTagController(mContext);
        mResolver = mContext.getContentResolver();

        tearDown();

        Uri tagUri = ProviderTestUtils.insertTag(mResolver, "_TEST_metalware");
        assertNotNull(tagUri);

        mMetalware = new Tag(tagUri.getLastPathSegment(), "_TEST_metalware");

        Uri milkProductTagUri = ProviderTestUtils.insertTag(mResolver, TEST_MILK_PRODUCT);
        assertNotNull(milkProductTagUri);

        mMilkProduct = new Tag(milkProductTagUri.getLastPathSegment(), TEST_MILK_PRODUCT);

        assertNotNull(mMetalware);
        assertNotNull(mMilkProduct);

        Uri productUri = ProviderTestUtils.insertProduct(mResolver, "_TEST_cheese", 1.0f, 1.0f, null);
        assertNotNull(productUri);

        mCheese = new Product(productUri.getLastPathSegment(), "_TEST_cheese",null, 1.0f, 1.0f);

        Uri cheeseMilkProductTagUri = ProviderTestUtils.insertTaggedProduct(mResolver, mCheese.mUUID, mMilkProduct.mUUID);
        assertNotNull(cheeseMilkProductTagUri);

        mCheeseMilkProductTag  = new TaggedProduct(cheeseMilkProductTagUri.getLastPathSegment(), mMilkProduct, mCheese);

    }

    public void tearDown() throws Exception {
        mResolver.delete(Uri.parse(TaggedProductProvider.MULTIPLE_TAGGED_PRODUCT_CONTENT_URI), null, null);
        mResolver.delete(Uri.parse(ProductProvider.MULTIPLE_PRODUCT_CONTENT_URI), null, null);
        mResolver.delete(Uri.parse(TagProvider.MULTIPLE_TAG_CONTENT_URI), null, null);
    }

    public void testCreateTag() throws Exception {
        assertNull(mTagController.createTag(null));
        assertNull(mTagController.createTag(TEST_MILK_PRODUCT));

        Tag createdTag = mTagController.createTag("_TEST_vegetable");
        assertNotNull(createdTag);
        assertEquals("_TEST_vegetable", createdTag.mName);
        assertEquals(createdTag, mTagController.findById(createdTag.mUUID));
    }

    public void testRenameTag() throws Exception {
        assertNull(mTagController.renameTag(null, "_TEST_somethingThatShouldNotWork"));
        assertEquals(mMilkProduct, mTagController.renameTag(mMilkProduct, null));
        assertEquals(mMilkProduct, mTagController.renameTag(mMilkProduct, "_TEST_metalware"));

        Tag renamedTag = mTagController.renameTag(mMilkProduct, "_TEST_vegetable");
        assertEquals("_TEST_vegetable", renamedTag.mName);
        assertEquals(renamedTag, mTagController.findById(mMilkProduct.mUUID));
    }

    public void testRemoveTag() throws Exception {
        mTagController.removeTag(mMilkProduct);
        assertNull(mTagController.findById(mCheeseMilkProductTag.mUUID));
        assertNotNull(mProductController.findById(mCheese.mUUID));
        assertNotNull(mTagController.findById(mMetalware.mUUID));
    }
}