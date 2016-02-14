package org.noorganization.instalist.presenter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import org.noorganization.instalist.presenter.implementation.ControllerFactory;
import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.provider.InstalistProvider;

import java.util.UUID;

public class ICategoryControllerTest extends AndroidTestCase {

    private ICategoryController mCategoryController;
    private Category mCategoryWork;
    private ShoppingList mListHardwareStore;
    private ContentResolver mResolver;

    public void setUp() throws Exception {
        super.setUp();
        mResolver = getContext().getContentResolver();
        tearDown();

        ContentValues testWorkCatCV = new ContentValues(1);
        testWorkCatCV.put(Category.COLUMN.NAME, "_TEST_work");
        Uri createdCat = mResolver.insert(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category"),
                testWorkCatCV);


        mCategoryWork = new Category(createdCat.getLastPathSegment(), "_TEST_work");
        ContentValues testHWStoreListCV = new ContentValues(2);
        testHWStoreListCV.put(ShoppingList.COLUMN.NAME, "_TEST_hardware store");
        testHWStoreListCV.put(ShoppingList.COLUMN.CATEGORY,
                createdCat.getLastPathSegment());
        Uri createdList = mResolver.insert(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category/" +
                        createdCat.getLastPathSegment() + "/list"),
                testHWStoreListCV);
        mListHardwareStore = new ShoppingList(createdList.getLastPathSegment(),
                "_TEST_hardware store", mCategoryWork);

        mCategoryController = ControllerFactory.getCategoryController(mContext);
    }

    public void tearDown() throws Exception {
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

    }

    public void testCreateCategory() throws Exception {
        assertNull(mCategoryController.createCategory(null));
        assertNull(mCategoryController.createCategory(""));
        assertNull(mCategoryController.createCategory("_TEST_work"));


        Category returnedCreatedCategory1 = mCategoryController.createCategory("_TEST_home");
        assertNotNull(returnedCreatedCategory1);
        assertEquals("_TEST_home", returnedCreatedCategory1.mName);
        assertNotNull(returnedCreatedCategory1.mUUID);
        Cursor catCheck = mResolver.query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category/" +
                        returnedCreatedCategory1.mUUID.toString()), null, null, null, null);
        assertEquals(1, catCheck.getCount());
        catCheck.moveToFirst();
        assertEquals("_TEST_home", catCheck.getString(catCheck.getColumnIndex(
                Category.COLUMN.NAME)));
        catCheck.close();
    }

    public void testGetCategoryById() throws Exception {
        //assertNull(mCategoryController.getCategoryByID(null));
        assertNull(mCategoryController.getCategoryByID(UUID.randomUUID().toString()));
        assertNull(mCategoryController.getCategoryByID(null));

        assertEquals(mCategoryWork, mCategoryController.getCategoryByID(mCategoryWork.mUUID));
    }

    public void testRenameCategory() throws Exception {
        assertNull(mCategoryController.renameCategory(null, ""));
        Category notSavedCategory = new Category(UUID.randomUUID().toString(), "_TEST_not saved category");
        assertNull(mCategoryController.renameCategory(notSavedCategory, "_TEST_still not saved category"));
        assertEquals(mCategoryWork, mCategoryController.renameCategory(mCategoryWork, ""));

        Category returnedRenamedCategory1 = mCategoryController.renameCategory(mCategoryWork, "_TEST_home");
        assertNotNull(returnedRenamedCategory1);
        assertEquals("_TEST_home", returnedRenamedCategory1.mName);
        Cursor catCheck = mResolver.query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category/" +
                        returnedRenamedCategory1.mUUID.toString()), null, null, null, null);
        assertEquals(1, catCheck.getCount());
        catCheck.moveToFirst();
        assertEquals("_TEST_home", catCheck.getString(catCheck.getColumnIndex(
                Category.COLUMN.NAME)));
        catCheck.close();
    }

    public void testRemoveCategory() throws Exception {
        // Nothing should happen if a wrong input is given.
        mCategoryController.removeCategory(null);
        Category notSavedCategory = new Category(UUID.randomUUID().toString(), "_TEST_not saved category");
        mCategoryController.removeCategory(notSavedCategory);

        // If a category gets deleted, lists have to be unlinked.
        String deletedId = mCategoryWork.mUUID;
        mCategoryController.removeCategory(mCategoryWork);
        Cursor catCheck = mResolver.query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category/" +
                        deletedId), null, null, null, null);
        assertNotNull(catCheck);
        assertEquals(0, catCheck.getCount());
        catCheck.close();

        Cursor listCheck = mResolver.query(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI,
                "category/-/list/" + mListHardwareStore.mUUID), null, null, null, null);
        assertNotNull(listCheck);
        assertEquals(1, listCheck.getCount());
        listCheck.moveToFirst();
        assertNull(listCheck.getString(listCheck.getColumnIndex(
                ShoppingList.COLUMN.CATEGORY)));
        listCheck.close();

    }
}