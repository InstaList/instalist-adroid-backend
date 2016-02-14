package org.noorganization.instalist.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

import org.noorganization.instalist.model.Ingredient;
import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.Recipe;
import org.noorganization.instalist.provider.internal.IInternalProvider;
import org.noorganization.instalist.provider.internal.ProductProvider;
import org.noorganization.instalist.provider.internal.RecipeProvider;

import java.util.UUID;

/**
 * Created by Tino on 28.10.2015.
 */
public class RecipeProviderTest extends AndroidTestCase {

    IInternalProvider mRecipeProvider;
    IInternalProvider mProductProvider;

    SQLiteDatabase mDatabase;

    @Override
    public void setUp() {
        mDatabase = new DBOpenHelper(getContext(), null).getWritableDatabase();
        mRecipeProvider = new RecipeProvider(getContext());
        mProductProvider = new ProductProvider(getContext());
        mRecipeProvider.onCreate(mDatabase);
        mProductProvider.onCreate(mDatabase);
    }

    @Override
    public void tearDown() {
        resetDb();
        mDatabase.close();
    }

    private void resetDb() {
        mDatabase.delete(Recipe.TABLE_NAME, null, null);
        mDatabase.delete(Product.TABLE_NAME, null, null);
    }

    public void testQueryMultipleRecipes() {
        Uri multipleRecipesUri = Uri.parse(RecipeProvider.MULTIPLE_RECIPE_CONTENT_URI);
        Cursor noProducts = mRecipeProvider.query(multipleRecipesUri, Recipe.COLUMN_PREFIXED.ALL_COLUMNS, null, null, null);
        assertNotNull(noProducts);
        assertEquals(0, noProducts.getCount());

        String uuid = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO " + Recipe.TABLE_NAME + " VALUES (?,?)", new String[]{
                uuid,
                "TestRecipe1"
        });

        Cursor productCursor = mRecipeProvider.query(multipleRecipesUri, Recipe.COLUMN_PREFIXED.ALL_COLUMNS, null, null, null);
        assertNotNull(productCursor);
        assertEquals(1, productCursor.getCount());
        productCursor.moveToFirst();
        assertEquals("TestRecipe1", productCursor.getString(productCursor.getColumnIndex(Recipe.COLUMN_PREFIXED.NAME)));
        assertEquals(uuid, productCursor.getString(productCursor.getColumnIndex(Recipe.COLUMN_PREFIXED.ID)));

        String uuid2 = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO " + Recipe.TABLE_NAME + " VALUES (?,?)", new String[]{
                uuid2,
                "TestRecipe2"
        });

        productCursor = mRecipeProvider.query(multipleRecipesUri, Recipe.COLUMN_PREFIXED.ALL_COLUMNS, null, null, Recipe.COLUMN_PREFIXED.NAME + " ASC");
        assertNotNull(productCursor);
        assertEquals(2, productCursor.getCount());
        productCursor.moveToFirst();
        assertEquals("TestRecipe1", productCursor.getString(productCursor.getColumnIndex(Recipe.COLUMN_PREFIXED.NAME)));
        assertEquals(uuid, productCursor.getString(productCursor.getColumnIndex(Recipe.COLUMN_PREFIXED.ID)));
        productCursor.moveToNext();
        assertEquals("TestRecipe2", productCursor.getString(productCursor.getColumnIndex(Recipe.COLUMN_PREFIXED.NAME)));
        assertEquals(uuid2, productCursor.getString(productCursor.getColumnIndex(Recipe.COLUMN_PREFIXED.ID)));
        resetDb();
    }

    public void testQuerySingleRecipe() {
        Cursor noCategory = mRecipeProvider.query(Uri.parse(RecipeProvider.SINGLE_RECIPE_CONTENT_URI.replace("*",
                UUID.randomUUID().toString())), Recipe.COLUMN_PREFIXED.ALL_COLUMNS, null, null, null);

        assertNotNull(noCategory);
        assertEquals(0, noCategory.getCount());

        String uuid = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO " + Recipe.TABLE_NAME + " VALUES (?,?)", new String[]{
                uuid,
                "TestRecipe1"
        });

        Cursor productCursor = mRecipeProvider.query(Uri.parse(RecipeProvider.SINGLE_RECIPE_CONTENT_URI.replace("*",
                uuid)), Recipe.COLUMN_PREFIXED.ALL_COLUMNS, null, null, null);
        assertNotNull(productCursor);
        assertEquals(1, productCursor.getCount());
        productCursor.moveToFirst();
        assertEquals("TestRecipe1", productCursor.getString(productCursor.getColumnIndex(Recipe.COLUMN_PREFIXED.NAME)));
        assertEquals(uuid, productCursor.getString(productCursor.getColumnIndex(Recipe.COLUMN_PREFIXED.ID)));

        String uuid2 = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO " + Recipe.TABLE_NAME + " VALUES (?,?)", new String[]{
                uuid2,
                "TestRecipe2"
        });

        productCursor = mRecipeProvider.query(Uri.parse(RecipeProvider.SINGLE_RECIPE_CONTENT_URI.replace("*", uuid2)),
                Recipe.COLUMN_PREFIXED.ALL_COLUMNS, null, null, null);
        assertNotNull(productCursor);
        assertEquals(1, productCursor.getCount());
        productCursor.moveToFirst();
        assertEquals("TestRecipe2", productCursor.getString(productCursor.getColumnIndex(Recipe.COLUMN_PREFIXED.NAME)));
        assertEquals(uuid2, productCursor.getString(productCursor.getColumnIndex(Recipe.COLUMN_PREFIXED.ID)));
        resetDb();
    }

    public void testGetTypeSingleRecipe() {
        String type = mRecipeProvider.getType(Uri.parse(RecipeProvider.SINGLE_RECIPE_CONTENT_URI.replace("*", "0")));
        assertEquals(ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + RecipeProvider.RECIPE_BASE_TYPE, type);

        type = mRecipeProvider.getType(Uri.parse(RecipeProvider.SINGLE_RECIPE_INGREDIENT_CONTENT_URI.replaceAll("\\*", "0")));
        assertEquals(ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + RecipeProvider.INGREDIENT_BASE_TYPE, type);
    }

    public void testGetTypeMultipleRecipes() {
        String type = mRecipeProvider.getType(Uri.parse(RecipeProvider.MULTIPLE_RECIPE_CONTENT_URI));
        assertEquals(type, ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + RecipeProvider.RECIPE_BASE_TYPE);
        type = mRecipeProvider.getType(Uri.parse(RecipeProvider.MULTIPLE_RECIPE_INGREDIENT_CONTENT_URI.replaceAll("\\*", "0")));
        assertEquals(ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + RecipeProvider.INGREDIENT_BASE_TYPE, type);
    }

    public void testInsertSingleRecipe() {
        ContentValues contentValues = new ContentValues();
        String uuid = UUID.randomUUID().toString();
        Uri uri = ProviderTestUtils.insertRecipe(mRecipeProvider, "TestRecipe");
        assertNotNull(uri);
        uuid = uri.getLastPathSegment();

        String pseudoUri = RecipeProvider.SINGLE_RECIPE_CONTENT_URI.replace("*", uuid);
        assertNotNull(uri);
        assertEquals(pseudoUri, uri.toString());

        Cursor cursor = mRecipeProvider.query(uri, Recipe.COLUMN_PREFIXED.ALL_COLUMNS, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals(uuid, cursor.getString(cursor.getColumnIndex(Recipe.COLUMN_PREFIXED.ID)));
        assertEquals("TestRecipe", cursor.getString(cursor.getColumnIndex(Recipe.COLUMN_PREFIXED.NAME)));
        resetDb();
    }


    public void testDeleteSingleRecipe() {

        ContentValues contentValues = new ContentValues();
        String uuid;
        Uri uri = ProviderTestUtils.insertRecipe(mRecipeProvider, "TestRecipe");
        assertNotNull(uri);

        uuid = uri.getLastPathSegment();

        int affectedRows = mRecipeProvider.delete(uri, null, null);
        assertEquals(1, affectedRows);
        Cursor cursor = mRecipeProvider.query(uri, Recipe.COLUMN_PREFIXED.ALL_COLUMNS, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
    }

    public void testDeleteMultipleRecipes() {

        String uuid;
        String uuid2;

        Uri uri = ProviderTestUtils.insertRecipe(mRecipeProvider, "TestRecipe1");
        Uri uri2 = ProviderTestUtils.insertRecipe(mRecipeProvider, "TestRecipe2");

        assertNotNull(uri);
        assertNotNull(uri2);

        uuid = uri.getLastPathSegment();
        uuid2 = uri2.getLastPathSegment();

        int affectedRows = mRecipeProvider.delete(Uri.parse(RecipeProvider.MULTIPLE_RECIPE_CONTENT_URI), Recipe.COLUMN_PREFIXED.NAME + " LIKE ?", new String[]{"%TestRecipe%"});

        assertEquals(2, affectedRows);

        uri = ProviderTestUtils.insertRecipe(mRecipeProvider, "TestRecipe1");
        uri2 = ProviderTestUtils.insertRecipe(mRecipeProvider, "TestRecipe2");

        assertNotNull(uri);
        assertNotNull(uri2);

        uuid = uri.getLastPathSegment();
        uuid2 = uri2.getLastPathSegment();

        affectedRows = mRecipeProvider.delete(Uri.parse(RecipeProvider.MULTIPLE_RECIPE_CONTENT_URI), Recipe.COLUMN_PREFIXED.NAME + " LIKE ?", new String[]{"%TestRecipe1%"});

        assertEquals(1, affectedRows);
    }

    public void testUpdateSingleRecipe() {
        ContentValues contentValues = new ContentValues();
        String uuid;
        String uuid2;

        Uri uri = ProviderTestUtils.insertRecipe(mRecipeProvider, "TestRecipe1");
        Uri uri2 = ProviderTestUtils.insertRecipe(mRecipeProvider, "TestRecipe2");

        assertNotNull(uri);
        assertNotNull(uri2);

        uuid = uri.getLastPathSegment();
        uuid2 = uri2.getLastPathSegment();

        contentValues.put(Recipe.COLUMN.ID, uuid);
        contentValues.put(Recipe.COLUMN.NAME, "TestRecipe2");
        int affectedRows = mRecipeProvider.update(uri, contentValues, null, null);

        assertEquals(1, affectedRows);

        Cursor cursor = mRecipeProvider.query(uri, Recipe.COLUMN_PREFIXED.ALL_COLUMNS, null, null, null);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();

        assertEquals("TestRecipe2", cursor.getString(cursor.getColumnIndex(Recipe.COLUMN_PREFIXED.NAME)));
        cursor.close();
    }


    public void testInsertSingleIngredient() {
        String uuidRecipe;

        Uri uri = ProviderTestUtils.insertRecipe(mRecipeProvider, "TestRecipe1");

        assertNotNull(uri);

        uuidRecipe = uri.getLastPathSegment();


        ContentValues contentValuesProduct = new ContentValues();
        String uuidProduct;

        contentValuesProduct.put(Product.COLUMN.NAME, "TestProduct");
        contentValuesProduct.put(Product.COLUMN.DEFAULT_AMOUNT, 0.5f);
        contentValuesProduct.put(Product.COLUMN.STEP_AMOUNT, 0.5f);
        contentValuesProduct.put(Product.COLUMN.UNIT, (String) null);

        Uri productUri = mProductProvider.insert(Uri.parse(ProductProvider.MULTIPLE_PRODUCT_CONTENT_URI), contentValuesProduct);
        assertNotNull(productUri);
        uuidProduct = productUri.getLastPathSegment();

        String ingredientUuid;
        uri = Uri.parse(RecipeProvider.MULTIPLE_RECIPE_INGREDIENT_CONTENT_URI.replace("*", uuidRecipe));

        ContentValues ingredientValues = new ContentValues();
        ingredientValues.put(Ingredient.COLUMN.PRODUCT_ID, uuidProduct);
        ingredientValues.put(Ingredient.COLUMN.RECIPE_ID, uuidRecipe);
        ingredientValues.put(Ingredient.COLUMN.AMOUNT, 1.0f);

        Uri ingredientUri = mRecipeProvider.insert(uri, ingredientValues);
        assertNotNull(ingredientUri);
        ingredientUuid = ingredientUri.getLastPathSegment();

        uri = Uri.parse(RecipeProvider.SINGLE_RECIPE_INGREDIENT_CONTENT_URI.replaceFirst("\\*", uuidRecipe).replaceFirst("\\*", ingredientUuid));

        assertNotNull(ingredientUri);
        assertEquals(true, uri.compareTo(ingredientUri) == 0);

        Cursor cursor = mRecipeProvider.query(uri, Ingredient.COLUMN.ALL_COLUMNS, null, null, null);

        assertNotNull(cursor);
        // only one ingredient should be included
        assertEquals(1, cursor.getCount());

        cursor.moveToFirst();
        assertEquals(ingredientUuid, cursor.getString(cursor.getColumnIndex(Ingredient.COLUMN.ID)));

    }


    public void testQuerySingleIngredient() {
        String uuidRecipe = UUID.randomUUID().toString();

        mDatabase.execSQL("INSERT INTO " + Recipe.TABLE_NAME + " VALUES (?,?)", new String[]{uuidRecipe, "TestRecipe1"});
        String uuidProduct;

        Uri productUri = ProviderTestUtils.insertProduct(mProductProvider, "TestProduct", 0.5f, 0.5f, (String) null);
        assertNotNull(productUri);
        uuidProduct = productUri.getLastPathSegment();

        String uuid = UUID.randomUUID().toString();

        mDatabase.execSQL("INSERT INTO " + Ingredient.TABLE_NAME + " VALUES (?,?,?,?)", new String[]{uuid, uuidProduct, uuidRecipe, String.valueOf(1.0f)});

        // Uri ingredientUri = mTaggedProductProvider.insert(uri,ingredientValues);
        Uri uri = Uri.parse(RecipeProvider.SINGLE_RECIPE_INGREDIENT_CONTENT_URI.replaceFirst("\\*", uuidRecipe).replaceFirst("\\*", uuid));
        assertNotNull(uri);

        Cursor cursor = mRecipeProvider.query(uri, Ingredient.COLUMN.ALL_COLUMNS, null, null, null);

        assertNotNull(cursor);
        // only one ingredient should be included
        assertEquals(1, cursor.getCount());

        cursor.moveToFirst();
        assertEquals(uuid, cursor.getString(cursor.getColumnIndex(Ingredient.COLUMN.ID)));
        cursor.close();
    }

    public void testQueryMultipleIngredients() {
        String uuidRecipe = UUID.randomUUID().toString();

        mDatabase.execSQL("INSERT INTO " + Recipe.TABLE_NAME + " VALUES (?,?)", new String[]{uuidRecipe, "TestRecipe1"});

        ContentValues contentValuesProduct = new ContentValues();
        String uuidProduct1;
        String uuidProduct2;

        Uri productUri1 = ProviderTestUtils.insertProduct(mProductProvider, "TestProduct1", 0.5f, 0.5f, (String) null);
        Uri productUri2 = ProviderTestUtils.insertProduct(mProductProvider, "TestProduct2", 0.5f, 0.5f, (String) null);

        assertNotNull(productUri1);
        assertNotNull(productUri2);

        uuidProduct1 = productUri1.getLastPathSegment();
        uuidProduct2 = productUri2.getLastPathSegment();

        String uuid = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();

        mDatabase.execSQL("INSERT INTO " + Ingredient.TABLE_NAME + " VALUES (?,?,?,?)", new String[]{uuid, uuidProduct1, uuidRecipe, String.valueOf(1.0f)});
        mDatabase.execSQL("INSERT INTO " + Ingredient.TABLE_NAME + " VALUES (?,?,?,?)", new String[]{uuid2, uuidProduct2, uuidRecipe, String.valueOf(2.0f)});

        Uri uri = Uri.parse(RecipeProvider.MULTIPLE_RECIPE_INGREDIENT_CONTENT_URI.replaceFirst("\\*", uuidRecipe));

        Cursor cursor = mRecipeProvider.query(uri, Ingredient.COLUMN.ALL_COLUMNS, null, null, Ingredient.COLUMN.AMOUNT + " ASC");

        assertNotNull(cursor);
        // only one ingredient should be included
        assertEquals(2, cursor.getCount());

        cursor.moveToFirst();
        assertEquals(uuid, cursor.getString(cursor.getColumnIndex(Ingredient.COLUMN.ID)));
        assertEquals(uuidProduct1, cursor.getString(cursor.getColumnIndex(Ingredient.COLUMN.PRODUCT_ID)));
        assertTrue(cursor.moveToNext());
        assertEquals(uuid2, cursor.getString(cursor.getColumnIndex(Ingredient.COLUMN.ID)));
        assertEquals(uuidProduct2, cursor.getString(cursor.getColumnIndex(Ingredient.COLUMN.PRODUCT_ID)));
        cursor.close();
    }

    public void testDeleteSingleIngredient() {

        String uuidRecipe;
        String uuidProduct;
        String ingredientUuid;

        Uri uri = ProviderTestUtils.insertRecipe(mRecipeProvider, "TestRecipe1");
        assertNotNull(uri);
        uuidRecipe = uri.getLastPathSegment();

        Uri productUri1 = ProviderTestUtils.insertProduct(mProductProvider, "TestProduct", 0.5f, 0.5f, (String) null);
        assertNotNull(productUri1);
        uuidProduct = productUri1.getLastPathSegment();


        uri = Uri.parse(RecipeProvider.MULTIPLE_RECIPE_INGREDIENT_CONTENT_URI.replace("*", uuidRecipe));
        assertNotNull(uri);

        ContentValues ingredientValues = new ContentValues();
        ingredientValues.put(Ingredient.COLUMN.PRODUCT_ID, uuidProduct);
        ingredientValues.put(Ingredient.COLUMN.RECIPE_ID, uuidRecipe);
        ingredientValues.put(Ingredient.COLUMN.AMOUNT, 1.0f);

        Uri ingredientUri = mRecipeProvider.insert(uri, ingredientValues);
        assertNotNull(ingredientUri);
        ingredientUuid = ingredientUri.getLastPathSegment();

        assertNotNull(ingredientUri);

        int affectedRows = mRecipeProvider.delete(ingredientUri, null, null);

        assertEquals(1, affectedRows);
        uri = Uri.parse(RecipeProvider.MULTIPLE_RECIPE_INGREDIENT_CONTENT_URI.replaceFirst("\\*", uuidRecipe));

        // check if the entries are really removed.
        Cursor cursor = mRecipeProvider.query(uri, Ingredient.COLUMN.ALL_COLUMNS, null, null, Ingredient.COLUMN.AMOUNT + " ASC");
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    public void testDeleteMultipleIngredient() {

        ContentValues contentValues = new ContentValues();
        String uuidRecipe;

        Uri uri = ProviderTestUtils.insertRecipe(mRecipeProvider, "TestRecipe1");
        assertNotNull(uri);
        uuidRecipe = uri.getLastPathSegment();

        String uuidProduct;
        String uuidProduct2;

        Uri productUri1 = ProviderTestUtils.insertProduct(mProductProvider, "TestProduct", 0.5f, 0.5f, (String) null);
        Uri productUri2 = ProviderTestUtils.insertProduct(mProductProvider, "TestProduct2", 0.5f, 0.5f, (String) null);

        assertNotNull(productUri1);
        assertNotNull(productUri2);

        uuidProduct = productUri1.getLastPathSegment();
        uuidProduct2 = productUri2.getLastPathSegment();

        Uri uri1 = Uri.parse(RecipeProvider.MULTIPLE_RECIPE_INGREDIENT_CONTENT_URI.replace("*", uuidRecipe));
        Uri uri2 = Uri.parse(RecipeProvider.MULTIPLE_RECIPE_INGREDIENT_CONTENT_URI.replace("*", uuidRecipe));

        ContentValues ingredientValues = new ContentValues();
        ingredientValues.put(Ingredient.COLUMN.PRODUCT_ID, uuidProduct);
        ingredientValues.put(Ingredient.COLUMN.RECIPE_ID, uuidRecipe);
        ingredientValues.put(Ingredient.COLUMN.AMOUNT, 1.0f);

        Uri newIngredientUri = mRecipeProvider.insert(uri1, ingredientValues);
        ingredientValues.put(Ingredient.COLUMN.PRODUCT_ID, uuidProduct2);
        Uri newIngredientUri2 =mRecipeProvider.insert(uri2, ingredientValues);

        assertNotNull(newIngredientUri);
        assertNotNull(newIngredientUri2);

        Uri recipeUri = Uri.parse(RecipeProvider.MULTIPLE_RECIPE_INGREDIENT_CONTENT_URI.replace("*", uuidRecipe));
        int affectedRows = mRecipeProvider.delete(recipeUri, null, null);

        assertEquals(2, affectedRows);
        // check if the entries are really removed.
        Cursor cursor = mRecipeProvider.query(recipeUri, Ingredient.COLUMN.ALL_COLUMNS, null, null, Ingredient.COLUMN.AMOUNT + " ASC");
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    public void testUpdateSingleIngredient() {
        String uuidRecipe;
        Uri testRecipeUri = ProviderTestUtils.insertRecipe(mRecipeProvider, "TestRecipe1");
        assertNotNull(testRecipeUri);

        uuidRecipe = testRecipeUri.getLastPathSegment();

        String uuidProduct ;
        String uuidProduct2;

        Uri productUri1 = ProviderTestUtils.insertProduct(mProductProvider, "TestProduct", 0.5f, 0.5f, (String) null);
        Uri productUri2 = ProviderTestUtils.insertProduct(mProductProvider, "TestProduct2", 0.5f, 0.5f, (String) null);

        assertNotNull(productUri1);
        assertNotNull(productUri2);

        uuidProduct = productUri1.getLastPathSegment();
        uuidProduct2 = productUri2.getLastPathSegment();

        String ingredient1Uuid ;
        String ingredient2Uuid2;
        Uri uri1 = Uri.parse(RecipeProvider.MULTIPLE_RECIPE_INGREDIENT_CONTENT_URI.replace("*", uuidRecipe));
        Uri uri2 = Uri.parse(RecipeProvider.MULTIPLE_RECIPE_INGREDIENT_CONTENT_URI.replace("*", uuidRecipe));

        ContentValues ingredientValues = new ContentValues();
        ingredientValues.put(Ingredient.COLUMN.PRODUCT_ID, uuidProduct);
        ingredientValues.put(Ingredient.COLUMN.RECIPE_ID, uuidRecipe);
        ingredientValues.put(Ingredient.COLUMN.AMOUNT, 1.0f);

        Uri ingredient1Uri = mRecipeProvider.insert(uri1, ingredientValues);

        ingredientValues.put(Ingredient.COLUMN.PRODUCT_ID, uuidProduct2);
        Uri ingredient2Uri = mRecipeProvider.insert(uri2, ingredientValues);

        assertNotNull(ingredient1Uri);
        assertNotNull(ingredient2Uri);

        ingredient1Uuid = ingredient1Uri.getLastPathSegment();
        ingredient2Uuid2 = ingredient2Uri.getLastPathSegment();

        ingredientValues.put(Ingredient.COLUMN.AMOUNT, 5.0f);

        Uri ingredientUri = Uri.parse(RecipeProvider.SINGLE_RECIPE_INGREDIENT_CONTENT_URI.replaceFirst("\\*", uuidRecipe).replaceFirst("\\*", ingredient1Uuid));
        int affectedRows = mRecipeProvider.update(ingredientUri,
                ingredientValues, null, null
        );

        assertEquals(1, affectedRows);

        Cursor cursor = mRecipeProvider.query(ingredientUri, Ingredient.COLUMN.ALL_COLUMNS, null, null, null);
        assertNotNull(cursor);
        cursor.moveToFirst();
        assertEquals(ingredient1Uuid, cursor.getString(cursor.getColumnIndex(Ingredient.COLUMN.ID)));
        assertEquals(5.0f, cursor.getFloat(cursor.getColumnIndex(Ingredient.COLUMN.AMOUNT)));
        cursor.close();

    }
}
