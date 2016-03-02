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

package org.noorganization.instalist.provider.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.model.Ingredient;
import org.noorganization.instalist.model.ListEntry;
import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.Recipe;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.model.Tag;
import org.noorganization.instalist.model.TaggedProduct;
import org.noorganization.instalist.model.Unit;
import org.noorganization.instalist.provider.InstalistProvider;
import org.noorganization.instalist.provider.ProviderTestUtils;
import org.noorganization.instalist.provider.internal.IngredientProvider;
import org.noorganization.instalist.provider.internal.ProductProvider;
import org.noorganization.instalist.provider.internal.RecipeProvider;
import org.noorganization.instalist.provider.internal.TagProvider;
import org.noorganization.instalist.provider.internal.TaggedProductProvider;
import org.noorganization.instalist.provider.internal.UnitProvider;

/**
 * Tests for {@link org.noorganization.instalist.provider.ProviderTestUtils}
 * Created by tinos_000 on 16.11.2015.
 */
public class ProviderTestUtilsTest extends AndroidTestCase {

    private ContentResolver mResolver;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mResolver = mContext.getContentResolver();
        tearDown();
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mResolver.delete(Uri.parse(ProductProvider.MULTIPLE_PRODUCT_CONTENT_URI), null, null);
        mResolver.delete(Uri.parse(UnitProvider.MULTIPLE_UNIT_CONTENT_URI), null, null);
        mResolver.delete(Uri.parse(RecipeProvider.MULTIPLE_RECIPE_CONTENT_URI), null, null);
        mResolver.delete(Uri.parse(IngredientProvider.MULTIPLE_INGREDIENT_CONTENT_URI), null, null);
        mResolver.delete(Uri.parse(TagProvider.MULTIPLE_TAG_CONTENT_URI), null, null);
        mResolver.delete(Uri.parse(TaggedProductProvider.MULTIPLE_TAGGED_PRODUCT_CONTENT_URI), null, null);

        Cursor categoryCursor = mResolver.query(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI,
                "category"), Category.COLUMN.ALL_COLUMNS, null, null, null);

        if (categoryCursor == null) {
            throw new IllegalStateException("No Category cursor found.");
        }

        if (categoryCursor.getCount() > 0) {
            categoryCursor.moveToFirst();
            do {
                String categoryId = categoryCursor.getString(categoryCursor.getColumnIndex(Category.COLUMN.ID));
                mResolver.delete(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category/" + categoryId), null, null);
            } while (categoryCursor.moveToNext());
        }
        categoryCursor.close();

        Cursor listsToDel = mResolver.query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "list"),
                new String[]{ShoppingList.COLUMN.ID, ShoppingList.COLUMN.CATEGORY},
                ShoppingList.COLUMN.NAME + " LIKE '_TEST_%'",
                null,
                null);
        assertNotNull(listsToDel);
        listsToDel.moveToFirst();
        while (!listsToDel.isAfterLast()) {
            String listUUIDStr = listsToDel.getString(listsToDel.getColumnIndex(
                    ShoppingList.COLUMN.ID));
            String catUUIDStr = listsToDel.getString(listsToDel.getColumnIndex(
                    ShoppingList.COLUMN.CATEGORY));
            if (catUUIDStr == null) {
                catUUIDStr = "-";
            }
            mResolver.delete(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI,
                    "category/" + catUUIDStr + "/list/" + listUUIDStr), null, null);
            listsToDel.moveToNext();
        }
        listsToDel.close();
    }

    public void testInsertUnit() {
        Uri newUnitUri = ProviderTestUtils.insertUnit(mResolver, "g");
        assertNotNull(newUnitUri);

        Cursor unitCursor = mResolver.query(newUnitUri, Unit.COLUMN.ALL_COLUMNS, null, null, null);
        assertNotNull(unitCursor);
        assertEquals(1, unitCursor.getCount());
        unitCursor.moveToFirst();
        assertEquals("g", unitCursor.getString(unitCursor.getColumnIndex(Unit.COLUMN.NAME)));
        unitCursor.close();
    }

    public void testGetUnit() {
        Uri newUnitUri = ProviderTestUtils.insertUnit(mResolver, "g");
        assertNotNull(newUnitUri);

        Unit unit = ProviderTestUtils.getUnit(mResolver, newUnitUri);
        assertNotNull(unit);
        assertEquals("g", unit.mName);
    }

    public void testInsertTag() {
        Uri newTagUri = ProviderTestUtils.insertTag(mResolver, "vegetable");
        assertNotNull(newTagUri);

        Cursor tagCursor = mResolver.query(newTagUri, Tag.COLUMN.ALL_COLUMNS, null, null, null);
        assertNotNull(tagCursor);
        assertEquals(1, tagCursor.getCount());
        tagCursor.moveToFirst();
        assertEquals("vegetable", tagCursor.getString(tagCursor.getColumnIndex(Tag.COLUMN.NAME)));
        tagCursor.close();
    }

    public void testGetTag() {
        Uri newTagUri = ProviderTestUtils.insertTag(mResolver, "vegetable");
        assertNotNull(newTagUri);

        Tag unit = ProviderTestUtils.getTag(mResolver, newTagUri);
        assertNotNull(unit);
        assertEquals("vegetable", unit.mName);
    }

    public void testInsertCategory() {
        Uri newCategoryUri = ProviderTestUtils.insertCategory(mResolver, "TEST_CATEGORY");
        assertNotNull(newCategoryUri);

        Cursor tagCursor = mResolver.query(newCategoryUri, Category.COLUMN.ALL_COLUMNS, null, null, null);
        assertNotNull(tagCursor);
        assertEquals(1, tagCursor.getCount());
        tagCursor.moveToFirst();
        assertEquals("TEST_CATEGORY", tagCursor.getString(tagCursor.getColumnIndex(Category.COLUMN.NAME)));
        tagCursor.close();
    }

    public void testInsertList() {
        Uri newCategoryUri = ProviderTestUtils.insertCategory(mResolver, "TEST_CATEGORY");
        assertNotNull(newCategoryUri);

        Uri newListUri = ProviderTestUtils.insertList(mResolver, newCategoryUri.getLastPathSegment(), "TEST_LIST");
        assertNotNull(newListUri);

        Cursor listCursor = mResolver.query(newListUri, ShoppingList.COLUMN.ALL_COLUMNS, null, null, null);
        assertNotNull(listCursor);
        assertEquals(1, listCursor.getCount());
        listCursor.moveToFirst();
        assertEquals("TEST_LIST", listCursor.getString(listCursor.getColumnIndex(ShoppingList.COLUMN.NAME)));
        listCursor.close();
    }

    public void testInserListEntry() {
        Uri newCategoryUri = ProviderTestUtils.insertCategory(mResolver, "TEST_CATEGORY");
        assertNotNull(newCategoryUri);

        Uri newListUri = ProviderTestUtils.insertList(mResolver, newCategoryUri.getLastPathSegment(), "TEST_LIST");
        assertNotNull(newListUri);

        Uri newProductUri = ProviderTestUtils.insertProduct(mResolver, "TEST_PRODUCT", 1.0f, 1.0f, null);
        assertNotNull(newProductUri);

        Uri listEntryUri = ProviderTestUtils.insertListEntry(mResolver,
                newCategoryUri.getLastPathSegment(),
                newListUri.getLastPathSegment(),
                newProductUri.getLastPathSegment(),
                3.0f,
                false,
                1);
        assertNotNull(listEntryUri);

        Cursor listEntryCursor = mResolver.query(listEntryUri, ListEntry.COLUMN.ALL_COLUMNS, null, null, null);

        assertNotNull(listEntryCursor);
        assertEquals(1, listEntryCursor.getCount());
        listEntryCursor.moveToFirst();
        assertEquals(false, listEntryCursor.getInt(listEntryCursor.getColumnIndex(ListEntry.COLUMN.STRUCK)) == 1);
        assertEquals(1, listEntryCursor.getInt(listEntryCursor.getColumnIndex(ListEntry.COLUMN.PRIORITY)));
        assertEquals(newProductUri.getLastPathSegment(), listEntryCursor.getString(listEntryCursor.getColumnIndex(ListEntry.COLUMN.PRODUCT)));
        assertEquals(newListUri.getLastPathSegment(), listEntryCursor.getString(listEntryCursor.getColumnIndex(ListEntry.COLUMN.LIST)));
        assertEquals(3.0f, listEntryCursor.getFloat(listEntryCursor.getColumnIndex(ListEntry.COLUMN.AMOUNT)), 0.001f);
        listEntryCursor.close();
    }

    public void testInsertRecipe() {
        Uri recipeUri = ProviderTestUtils.insertRecipe(mResolver, "TEST_RECIPE");
        assertNotNull(recipeUri);

        Cursor recipeCursor = mResolver.query(recipeUri, Recipe.COLUMN.ALL_COLUMNS, null, null, null);
        assertNotNull(recipeCursor);
        assertEquals(1, recipeCursor.getCount());
        recipeCursor.moveToFirst();
        assertEquals(recipeUri.getLastPathSegment(), recipeCursor.getString(recipeCursor.getColumnIndex(Recipe.COLUMN.ID)));
        assertEquals("TEST_RECIPE", recipeCursor.getString(recipeCursor.getColumnIndex(Recipe.COLUMN.NAME)));
        recipeCursor.close();
    }

    public void testInsertIngredient() {
        Uri productUri = ProviderTestUtils.insertProduct(mResolver, "TEST_PRODUCT", 1.0f, 1.0f, null);
        assertNotNull(productUri);
        Uri recipeUri = ProviderTestUtils.insertRecipe(mResolver, "TEST_RECIPE");
        assertNotNull(recipeUri);

        String productUUID = productUri.getLastPathSegment();
        String recipueUUID = recipeUri.getLastPathSegment();

        Uri ingredientUri = ProviderTestUtils.insertIngredient(mResolver, recipueUUID, productUUID, 1.0f);
        assertNotNull(ingredientUri);

        Cursor ingredientCursor = mResolver.query(ingredientUri, Ingredient.COLUMN.ALL_COLUMNS, null, null, null);
        assertNotNull(ingredientCursor);
        assertEquals(1, ingredientCursor.getCount());
        ingredientCursor.moveToFirst();

        assertEquals(ingredientUri.getLastPathSegment(), ingredientCursor.getString(ingredientCursor.getColumnIndex(Ingredient.COLUMN.ID)));
        assertEquals(productUUID, ingredientCursor.getString(ingredientCursor.getColumnIndex(Ingredient.COLUMN.PRODUCT_ID)));
        assertEquals(recipueUUID, ingredientCursor.getString(ingredientCursor.getColumnIndex(Ingredient.COLUMN.RECIPE_ID)));
        assertEquals(1.0f, ingredientCursor.getFloat(ingredientCursor.getColumnIndex(Ingredient.COLUMN.AMOUNT)));

        ingredientCursor.close();
    }

    public void testInsertProduct() {
        Uri newUnitUri = ProviderTestUtils.insertUnit(mResolver, "g");
        Uri insertedProductUri = ProviderTestUtils.insertProduct(mResolver, "TEST_PRODUCT", 1.0f, 0.5f, newUnitUri.getLastPathSegment());

        Cursor productCursor = mResolver.query(Uri.parse(ProductProvider.MULTIPLE_PRODUCT_CONTENT_URI), Product.COLUMN.ALL_COLUMNS, null, null, null);
        assertNotNull(productCursor);
        assertEquals(1, productCursor.getCount());

        productCursor.moveToFirst();
        assertEquals("TEST_PRODUCT", productCursor.getString(productCursor.getColumnIndex(Product.COLUMN.NAME)));
        assertEquals(1.0f, productCursor.getFloat(productCursor.getColumnIndex(Product.COLUMN.DEFAULT_AMOUNT)), 0.0001f);
        assertEquals(0.5f, productCursor.getFloat(productCursor.getColumnIndex(Product.COLUMN.STEP_AMOUNT)), 0.0001f);
        assertEquals(insertedProductUri.getLastPathSegment(), productCursor.getString(productCursor.getColumnIndex(Product.COLUMN.ID)));
        assertEquals(newUnitUri.getLastPathSegment(), productCursor.getString(productCursor.getColumnIndex(Product.COLUMN.UNIT)));

        productCursor.close();
    }

    public void testInsertTaggedProduct() {
        Uri newTag = ProviderTestUtils.insertTag(mResolver, "FRESH");
        assertNotNull(newTag);
        Uri insertedProductUri = ProviderTestUtils.insertProduct(mResolver, "TEST_PRODUCT", 1.0f, 0.5f, null);
        assertNotNull(insertedProductUri);

        Uri newTaggedProductUri = ProviderTestUtils.insertTaggedProduct(mResolver, insertedProductUri.getLastPathSegment(), newTag.getLastPathSegment());

        assertNotNull(newTaggedProductUri);

        Cursor taggedProductCursor = mResolver.query(newTaggedProductUri,
                new String[]{TaggedProduct.COLUMN_PREFIXED.ID, TaggedProduct.COLUMN_PREFIXED.PRODUCT_ID, TaggedProduct.COLUMN_PREFIXED.TAG_ID},
                null, null, null);

        assertNotNull(taggedProductCursor);
        assertEquals(1, taggedProductCursor.getCount());

        taggedProductCursor.moveToFirst();

        assertEquals(newTaggedProductUri.getLastPathSegment(), taggedProductCursor.getString(taggedProductCursor.getColumnIndex(TaggedProduct.COLUMN_PREFIXED.ID)));
        assertEquals(insertedProductUri.getLastPathSegment(), taggedProductCursor.getString(taggedProductCursor.getColumnIndex(TaggedProduct.COLUMN_PREFIXED.PRODUCT_ID)));
        assertEquals(newTag.getLastPathSegment(), taggedProductCursor.getString(taggedProductCursor.getColumnIndex(TaggedProduct.COLUMN_PREFIXED.TAG_ID)));

        taggedProductCursor.close();
    }

    public void testInsertProductWithoutUnit() {
        Uri insertedProductUri = ProviderTestUtils.insertProduct(mResolver, "TEST_PRODUCT", 1.0f, 0.5f, null);

        Cursor productCursor = mResolver.query(Uri.parse(ProductProvider.MULTIPLE_PRODUCT_CONTENT_URI), Product.COLUMN.ALL_COLUMNS, null, null, null);
        assertNotNull(productCursor);
        assertEquals(1, productCursor.getCount());

        productCursor.moveToFirst();
        assertEquals("TEST_PRODUCT", productCursor.getString(productCursor.getColumnIndex(Product.COLUMN.NAME)));
        assertEquals(1.0f, productCursor.getFloat(productCursor.getColumnIndex(Product.COLUMN.DEFAULT_AMOUNT)), 0.0001f);
        assertEquals(0.5f, productCursor.getFloat(productCursor.getColumnIndex(Product.COLUMN.STEP_AMOUNT)), 0.0001f);
        assertEquals(insertedProductUri.getLastPathSegment(), productCursor.getString(productCursor.getColumnIndex(Product.COLUMN.ID)));
        assertEquals(null, productCursor.getString(productCursor.getColumnIndex(Product.COLUMN.UNIT)));

        productCursor.close();
    }

    public void testGetProduct() {
        Uri newUnitUri = ProviderTestUtils.insertUnit(mResolver, "g");
        Uri insertedProduct = ProviderTestUtils.insertProduct(mResolver, "TEST_PRODUCT", 1.0f, 0.5f, newUnitUri.getLastPathSegment());

        Product product = ProviderTestUtils.getProduct(mResolver, insertedProduct.getLastPathSegment());
        assertEquals("TEST_PRODUCT", product.mName);
        assertEquals(1.0f, product.mDefaultAmount, 0.0001f);
        assertEquals(0.5f, product.mStepAmount, 0.0001f);
        assertEquals(newUnitUri.getLastPathSegment(), product.mUnit.mUUID);
    }


}
