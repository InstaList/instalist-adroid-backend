package org.noorganization.instalist.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.model.ListEntry;
import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.provider.internal.CategoryProvider;
import org.noorganization.instalist.provider.internal.IInternalProvider;

import java.util.UUID;

/**
 * Created by damihe on 24.10.15.
 */
public class CategoryProviderTest extends AndroidTestCase {

    IInternalProvider mCategoryProvider;
    SQLiteDatabase mDatabase;

    @Override
    public void setUp() {
        mDatabase = new DBOpenHelper(getContext(), null).getWritableDatabase();
        mCategoryProvider = new CategoryProvider();
        mCategoryProvider.onCreate(mDatabase);
    }

    @Override
    public void tearDown() {
        mDatabase.delete(Category.TABLE_NAME, null, null);
        mDatabase.close();
    }

    public void testQueryMultipleCategories() {
        Uri MULTIPLE_CATEGORIES = Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI,
                "category");
        Cursor noCategories = mCategoryProvider.query(MULTIPLE_CATEGORIES, null, null, null, null);
        assertNotNull(noCategories);
        assertEquals(0, noCategories.getCount());
        noCategories.close();

        String stCategory = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO category VALUES (?,?)", new String[]{
                stCategory,
                "TestCategory1"
        });
        Cursor oneCategory = mCategoryProvider.query(MULTIPLE_CATEGORIES, null, null, null, null);
        assertNotNull(oneCategory);
        assertEquals(1, oneCategory.getCount());
        oneCategory.moveToFirst();
        assertEquals("TestCategory1", oneCategory.getString(oneCategory.getColumnIndex("name")));
        assertEquals(stCategory, oneCategory.getString(oneCategory.getColumnIndex("_id")));

        String ndCategory = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO category VALUES (?,?)", new String[]{
                ndCategory,
                "TestCategory2"
        });
        Cursor twoCategories = mCategoryProvider.query(MULTIPLE_CATEGORIES, null, null, null, "name ASC");
        assertNotNull(twoCategories);
        assertEquals(2, twoCategories.getCount());
        twoCategories.moveToFirst();
        assertEquals("TestCategory1", twoCategories.getString(twoCategories.getColumnIndex("name")));
        assertEquals(stCategory, twoCategories.getString(twoCategories.getColumnIndex("_id")));
        twoCategories.moveToNext();
        assertEquals("TestCategory2", twoCategories.getString(twoCategories.getColumnIndex("name")));
        assertEquals(ndCategory, twoCategories.getString(twoCategories.getColumnIndex("_id")));
    }

    public void testQuerySingleCategory() {
        String SINGLE_CATEGORY = "content://" + InstalistProvider.AUTHORITY + "/category/%s";
        Cursor noCategory = mCategoryProvider.query(Uri.parse(String.format(SINGLE_CATEGORY,
                UUID.randomUUID().toString())), null, null, null, null);
        assertNotNull(noCategory);
        assertEquals(0, noCategory.getCount());

        String stCategory = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO category VALUES (?,?)", new String[]{
                stCategory,
                "TestCategory1"
        });
        Cursor oneCategory = mCategoryProvider.query(Uri.parse(String.format(SINGLE_CATEGORY,
                stCategory)), null, null, null, null);
        assertNotNull(oneCategory);
        assertEquals(1, oneCategory.getCount());
        oneCategory.moveToFirst();
        assertEquals("TestCategory1", oneCategory.getString(oneCategory.getColumnIndex("name")));
        assertEquals(stCategory, oneCategory.getString(oneCategory.getColumnIndex("_id")));

        String ndCategory = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO category VALUES (?,?)", new String[]{
                ndCategory,
                "TestCategory2"
        });
        Cursor twoCategories = mCategoryProvider.query(Uri.parse(String.format(SINGLE_CATEGORY,
                ndCategory)), null, null, null, null);
        assertNotNull(twoCategories);
        assertEquals(1, twoCategories.getCount());
        twoCategories.moveToFirst();
        assertEquals("TestCategory2", twoCategories.getString(twoCategories.getColumnIndex("name")));
        assertEquals(ndCategory, twoCategories.getString(twoCategories.getColumnIndex("_id")));
    }

    public void testQueryMultipleLists() {
        Uri MULTIPLE_LISTS_NO_CATEGORY = Uri.parse("content://" + InstalistProvider.AUTHORITY +
                "/category/-/list");
        String categoryUUID = UUID.randomUUID().toString();
        Uri MULTIPLE_LISTS_IN_CATEGORY = Uri.parse("content://" + InstalistProvider.AUTHORITY +
                "/category/" + categoryUUID + "/list");

        Cursor notExistentCategory = mCategoryProvider.query(MULTIPLE_LISTS_IN_CATEGORY, null, null,
                null, null);
        assertNotNull(notExistentCategory);
        assertEquals(0, notExistentCategory.getCount());

        String noCatListUUID = UUID.randomUUID().toString();
        String inCatListUUID = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO category (_id, name) VALUES (?, ?)", new String[]{
                categoryUUID, "a category"
        });
        mDatabase.execSQL("INSERT INTO list (_id, name, category) VALUES (?, ?, NULL), (?, ?, ?)",
                new String[]{
                        noCatListUUID, "test list without category",
                        inCatListUUID, "test list with category", categoryUUID
                });
        Cursor listNoCategory = mCategoryProvider.query(MULTIPLE_LISTS_NO_CATEGORY, null, null,
                null, null);
        assertNotNull(listNoCategory);
        assertEquals(1, listNoCategory.getCount());
        listNoCategory.moveToFirst();
        assertEquals(noCatListUUID, listNoCategory.getString(listNoCategory.getColumnIndex("_id")));
        assertEquals("test list without category", listNoCategory.getString(listNoCategory.getColumnIndex("name")));
        assertNull(listNoCategory.getString(listNoCategory.getColumnIndex("category")));

        Cursor listInCategory = mCategoryProvider.query(MULTIPLE_LISTS_IN_CATEGORY, null, null,
                null, null);
        assertNotNull(listInCategory);
        assertEquals(1, listInCategory.getCount());
        listInCategory.moveToFirst();
        assertEquals(inCatListUUID, listInCategory.getString(listInCategory.getColumnIndex("_id")));
        assertEquals("test list with category", listInCategory.getString(listInCategory.getColumnIndex("name")));
        assertEquals(categoryUUID, listInCategory.getString(listInCategory.getColumnIndex("category")));

    }

    public void testQuerySingleList() {
        String SINGLE_LIST = "content://" + InstalistProvider.AUTHORITY + "/category/%s/list/%s";
        Cursor noList = mCategoryProvider.query(Uri.parse(String.format(SINGLE_LIST,
                UUID.randomUUID().toString(), UUID.randomUUID().toString())), null, null, null, null);
        assertNotNull(noList);
        assertEquals(0, noList.getCount());

        String stList = UUID.randomUUID().toString();
        String ndList = UUID.randomUUID().toString();
        String categoryUUID = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO category VALUES (?,?)", new String[]{
                categoryUUID,
                "TestCategory1"
        });
        mDatabase.execSQL("INSERT INTO list (_id, name, category) VALUES (?, ?, null), (?, ?, ?)",
                new String[]{
                        stList, "listWOCategory",
                        ndList, "listWCategory", categoryUUID
                });
        Cursor stListWOCategory = mCategoryProvider.query(Uri.parse(String.format(SINGLE_LIST,
                "-", stList)), null, null, null, null);
        assertNotNull(stListWOCategory);
        assertEquals(1, stListWOCategory.getCount());
        stListWOCategory.moveToFirst();
        assertEquals("listWOCategory", stListWOCategory.getString(stListWOCategory.getColumnIndex("name")));
        assertEquals(stList, stListWOCategory.getString(stListWOCategory.getColumnIndex("_id")));
        assertNull(stListWOCategory.getString(stListWOCategory.getColumnIndex("category")));

        Cursor ndListWCategory = mCategoryProvider.query(Uri.parse(String.format(SINGLE_LIST,
                categoryUUID, ndList)), null, null, null, null);
        assertNotNull(ndListWCategory);
        assertEquals(1, ndListWCategory.getCount());
        ndListWCategory.moveToFirst();
        assertEquals("listWCategory", ndListWCategory.getString(ndListWCategory.getColumnIndex("name")));
        assertEquals(ndList, ndListWCategory.getString(ndListWCategory.getColumnIndex("_id")));
        assertEquals(categoryUUID, ndListWCategory.getString(ndListWCategory.getColumnIndex("category")));

        Cursor stListWCategory = mCategoryProvider.query(Uri.parse(String.format(SINGLE_LIST,
                categoryUUID, stList)), null, null, null, null);
        assertNotNull(stListWCategory);
        assertEquals(0, stListWCategory.getCount());

        Cursor ndListWOCategory = mCategoryProvider.query(Uri.parse(String.format(SINGLE_LIST,
                "-", ndList)), null, null, null, null);
        assertNotNull(ndListWOCategory);
        assertEquals(0, ndListWOCategory.getCount());
    }

    public void testQueryMultipleListEntries() {
        String categoryUUID = UUID.randomUUID().toString();
        String listUUID = UUID.randomUUID().toString();
        Uri MULTIPLE_LIST_ELEMENTS = Uri.parse("content://" + InstalistProvider.AUTHORITY +
                "/category/" + categoryUUID + "/list/" + listUUID + "/entry");
        Uri MULTIPLE_LIST_ELEMENTS_WO_CAT = Uri.parse("content://" + InstalistProvider.AUTHORITY +
                "/category/-/list/" + listUUID + "/entry");

        mDatabase.execSQL("INSERT INTO category (_id, name) VALUES (?, ?)", new String[]{
                categoryUUID,
                "category"
        });
        mDatabase.execSQL("INSERT INTO list (_id, name, category) VALUES (?, ?, ?)", new String[]{
                listUUID,
                "list",
                categoryUUID
        });

        Cursor noEntries1 = mCategoryProvider.query(MULTIPLE_LIST_ELEMENTS, null, null, null, null);
        assertNotNull(noEntries1);
        assertEquals(0, noEntries1.getCount());

        String entry1stUUID = UUID.randomUUID().toString();
        String product1stUUID = UUID.randomUUID().toString();
        String product2ndUUID = UUID.randomUUID().toString();
        Log.i("TEST", "category: " + categoryUUID + " list: " + listUUID + " product: " + product1stUUID + " entry: " + entry1stUUID);
        mDatabase.execSQL("INSERT INTO product (_id, name, unit_id) VALUES (?, ?, NULL), (?, ?, NULL)", new String[]{
                product1stUUID,
                "product1",
                product2ndUUID,
                "product2"
        });
        mDatabase.execSQL("INSERT INTO listentry (_id, product, list) " +
                "VALUES (?, ?, ?)", new String[]{entry1stUUID, product1stUUID, listUUID});

        Cursor noEntries2 = mCategoryProvider.query(MULTIPLE_LIST_ELEMENTS_WO_CAT, null, null, null,
                null);
        assertNotNull(noEntries2);
        assertEquals(0, noEntries2.getCount());

        Cursor oneEntry = mCategoryProvider.query(MULTIPLE_LIST_ELEMENTS, null, null, null, null);
        assertNotNull(oneEntry);
        assertEquals(1, oneEntry.getCount());
        oneEntry.moveToFirst();
        assertEquals(entry1stUUID, oneEntry.getString(oneEntry.getColumnIndex("_id")));
        assertEquals(product1stUUID, oneEntry.getString(oneEntry.getColumnIndex("product")));
        assertEquals(listUUID, oneEntry.getString(oneEntry.getColumnIndex("list")));

        String entry2ndUUID = UUID.randomUUID().toString();
        mDatabase.execSQL("INSERT INTO listentry (_id, product, list) VALUES (?, ?, ?)", new String[]{
                entry2ndUUID,
                product2ndUUID,
                listUUID
        });
        Cursor twoEntries = mCategoryProvider.query(MULTIPLE_LIST_ELEMENTS, null, null, null,
                "product.name ASC");
        assertNotNull(twoEntries);
        assertEquals(2, twoEntries.getCount());
        twoEntries.moveToFirst();
        assertEquals(entry1stUUID, twoEntries.getString(twoEntries.getColumnIndex("_id")));
        assertEquals(product1stUUID, twoEntries.getString(twoEntries.getColumnIndex("product")));
        assertEquals(listUUID, twoEntries.getString(twoEntries.getColumnIndex("list")));
        twoEntries.moveToNext();
        assertEquals(entry2ndUUID, twoEntries.getString(twoEntries.getColumnIndex("_id")));
        assertEquals(product2ndUUID, twoEntries.getString(twoEntries.getColumnIndex("product")));
        assertEquals(listUUID, twoEntries.getString(twoEntries.getColumnIndex("list")));
    }

    public void testQuerySingleListEntries() {
        String categoryUUID = UUID.randomUUID().toString();
        String listUUID = UUID.randomUUID().toString();
        String entry1stUUID = UUID.randomUUID().toString();
        String entry2ndUUID = UUID.randomUUID().toString();
        String product1stUUID = UUID.randomUUID().toString();
        String product2ndUUID = UUID.randomUUID().toString();

        Uri SINGLE_LIST_ELEMENT1 = Uri.parse("content://" + InstalistProvider.AUTHORITY +
                "/category/" + categoryUUID + "/list/" + listUUID + "/entry/" + entry1stUUID);
        Uri SINGLE_LIST_ELEMENT2 = Uri.parse("content://" + InstalistProvider.AUTHORITY +
                "/category/" + categoryUUID + "/list/" + listUUID + "/entry/" + entry2ndUUID);
        Uri SINGLE_LIST_ELEMENT_WO_CAT = Uri.parse("content://" + InstalistProvider.AUTHORITY +
                "/category/-/list/" + listUUID + "/entry/" + entry1stUUID);

        mDatabase.execSQL("INSERT INTO category (_id, name) VALUES (?, ?)", new String[]{
                categoryUUID,
                "category"
        });
        mDatabase.execSQL("INSERT INTO list (_id, name, category) VALUES (?, ?, ?)", new String[]{
                listUUID,
                "list",
                categoryUUID
        });

        Cursor noEntry1 = mCategoryProvider.query(SINGLE_LIST_ELEMENT1, null, null, null, null);
        assertNotNull(noEntry1);
        assertEquals(0, noEntry1.getCount());

        mDatabase.execSQL("INSERT INTO product (_id, name) VALUES (?, ?), (?, ?)", new String[]{
                product1stUUID,
                "product1",
                product2ndUUID,
                "product2"
        });
        mDatabase.execSQL("INSERT INTO listentry (_id, product, list) " +
                "VALUES (?, ?, ?), (?, ?, ?)", new String[]{
                entry1stUUID,
                product1stUUID,
                listUUID,
                entry2ndUUID,
                product2ndUUID,
                listUUID
        });

        Cursor noEntry2 = mCategoryProvider.query(SINGLE_LIST_ELEMENT_WO_CAT, null, null, null,
                null);
        assertNotNull(noEntry2);
        assertEquals(0, noEntry2.getCount());

        Cursor oneEntry1 = mCategoryProvider.query(SINGLE_LIST_ELEMENT1, null, null, null, null);
        assertNotNull(oneEntry1);
        assertEquals(1, oneEntry1.getCount());
        oneEntry1.moveToFirst();
        assertEquals(entry1stUUID, oneEntry1.getString(oneEntry1.getColumnIndex("_id")));
        assertEquals(product1stUUID, oneEntry1.getString(oneEntry1.getColumnIndex("product")));
        assertEquals(listUUID, oneEntry1.getString(oneEntry1.getColumnIndex("list")));

        Cursor oneEntry2 = mCategoryProvider.query(SINGLE_LIST_ELEMENT2, null, null, null, null);
        assertNotNull(oneEntry2);
        assertEquals(1, oneEntry2.getCount());
        oneEntry2.moveToFirst();
        assertEquals(entry2ndUUID, oneEntry2.getString(oneEntry2.getColumnIndex("_id")));
        assertEquals(product2ndUUID, oneEntry2.getString(oneEntry2.getColumnIndex("product")));
        assertEquals(listUUID, oneEntry2.getString(oneEntry2.getColumnIndex("list")));
    }

    public void testGetType() {
        String typeDirPrefix = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + InstalistProvider.BASE_VENDOR;
        String typeItemPrefix = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + InstalistProvider.BASE_VENDOR;

        assertEquals(typeDirPrefix + "category",
                mCategoryProvider.getType(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category")));
        assertEquals(typeItemPrefix + "category",
                mCategoryProvider.getType(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category/-")));
        assertEquals(typeDirPrefix + "list",
                mCategoryProvider.getType(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category/-/list")));
        assertEquals(typeItemPrefix + "list",
                mCategoryProvider.getType(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category/-/list/-")));
        assertEquals(typeDirPrefix + "entry",
                mCategoryProvider.getType(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category/-/list/-/entry")));
        assertEquals(typeItemPrefix + "entry",
                mCategoryProvider.getType(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category/-/list/-/entry/-")));
    }

    public void testInsertCategory() {
        Uri categoryUri = Uri.parse("content://" + InstalistProvider.AUTHORITY + "/category");

        ContentValues category1CV = new ContentValues(1);
        category1CV.put("name", "category1");
        Uri rtndCat1Uri = mCategoryProvider.insert(categoryUri, category1CV);
        assertNotNull(rtndCat1Uri);
        Cursor createdCategory1 = mDatabase.rawQuery("SELECT _id, name FROM category", null);
        assertNotNull(createdCategory1);
        assertEquals(1, createdCategory1.getCount());
        createdCategory1.moveToFirst();
        assertEquals("category1", createdCategory1.getString(createdCategory1.getColumnIndex("name")));
        assertEquals(rtndCat1Uri.getLastPathSegment(), createdCategory1.getString(
                createdCategory1.getColumnIndex("_id")));
        createdCategory1.close();

        ContentValues category2CV = new ContentValues(0);
        Uri rtndCat2Uri = mCategoryProvider.insert(categoryUri, category2CV);
        assertNull(rtndCat2Uri);
    }

    public void testInsertList() {
        String categoryUUID = UUID.randomUUID().toString();
        Uri woCategoryUri = Uri.parse("content://" + InstalistProvider.AUTHORITY + "/category/-/list");
        Uri wCategoryUri = Uri.parse("content://" + InstalistProvider.AUTHORITY + "/category/" +
                categoryUUID + "/list");

        ContentValues listWOCatCV = new ContentValues(1);
        listWOCatCV.put("name", "list w/o category");
        Uri resultingLWOCURI = mCategoryProvider.insert(woCategoryUri, listWOCatCV);
        assertNotNull(resultingLWOCURI);
        String resultingLWOCUUID = resultingLWOCURI.getLastPathSegment();
        Cursor testCursor = mDatabase.rawQuery("SELECT _id, name, category FROM list", null);
        assertEquals(1, testCursor.getCount());
        testCursor.moveToFirst();
        assertEquals(resultingLWOCUUID, testCursor.getString(testCursor.getColumnIndex("_id")));
        assertEquals("list w/o category", testCursor.getString(testCursor.getColumnIndex("name")));
        assertNull(testCursor.getString(testCursor.getColumnIndex("category")));
        testCursor.close();

        ContentValues wrongListWCatCV = new ContentValues(1);
        wrongListWCatCV.put("name", "wrong list w category");
        assertNull(mCategoryProvider.insert(wCategoryUri, wrongListWCatCV));
        testCursor = mDatabase.rawQuery("SELECT _id, name, category FROM list", null);
        assertEquals(1, testCursor.getCount());
        testCursor.close();

        mDatabase.execSQL("INSERT INTO category (_id, name) VALUES (?, 'cat1')", new String[]{
                categoryUUID
        });
        ContentValues listWCatCV = new ContentValues(1);
        listWCatCV.put("name", "list w category");
        Uri resultingLWCUri = mCategoryProvider.insert(wCategoryUri, listWCatCV);
        assertNotNull(resultingLWCUri);
        String resultingLWCUUID = resultingLWCUri.getLastPathSegment();
        testCursor = mDatabase.rawQuery("SELECT name, category FROM list WHERE _id = ?",
                new String[]{resultingLWCUUID});
        assertEquals(1, testCursor.getCount());
        testCursor.moveToFirst();
        assertEquals("list w category", testCursor.getString(testCursor.getColumnIndex("name")));
        assertEquals(categoryUUID, testCursor.getString(testCursor.getColumnIndex("category")));
        testCursor.close();
    }

    public void testInsertEntry() {
        String listUUID = UUID.randomUUID().toString();
        String productUUID = UUID.randomUUID().toString();
        Uri entryUri = Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI,
                "/category/-/list/" + listUUID + "/entry");

        ContentValues negativeEntryWOList = new ContentValues(1);
        negativeEntryWOList.put(ListEntry.COLUMN.PRODUCT, productUUID);
        assertNull(mCategoryProvider.insert(entryUri, negativeEntryWOList));
        Cursor testCursor = mDatabase.rawQuery("SELECT " + ListEntry.COLUMN.ID + " FROM " +
                ListEntry.TABLE_NAME, null);
        assertEquals(0, testCursor.getCount());
        testCursor.close();

        mDatabase.execSQL("INSERT INTO list (_id, name, category) VALUES (?, 'list w/o category', null)",
                new String[]{listUUID});
        ContentValues negativeEntryWOProduct = new ContentValues(1);
        negativeEntryWOProduct.put(ListEntry.COLUMN.PRODUCT, productUUID);
        assertNull(mCategoryProvider.insert(entryUri, negativeEntryWOProduct));
        testCursor = mDatabase.rawQuery("SELECT " + ListEntry.COLUMN.ID + " FROM " +
                ListEntry.TABLE_NAME, null);
        assertEquals(0, testCursor.getCount());
        testCursor.close();

        mDatabase.execSQL("INSERT INTO " + Product.TABLE_NAME + " (" + Product.COLUMN.ID + ", " +
                Product.COLUMN.NAME + ") VALUES (?, 'product 1')", new String[]{productUUID});

        ContentValues entryMinimumCV = new ContentValues(1);
        entryMinimumCV.put(ListEntry.COLUMN.PRODUCT, productUUID);
        Uri resultingMinimumEntryURI = mCategoryProvider.insert(entryUri, entryMinimumCV);
        assertNotNull(resultingMinimumEntryURI);
        String resultingMinimumEntryUUID = resultingMinimumEntryURI.getLastPathSegment();
        testCursor = mDatabase.rawQuery("SELECT " + ListEntry.COLUMN.ID + ", " +
                ListEntry.COLUMN.PRODUCT + ", " + ListEntry.COLUMN.LIST + " FROM " +
                ListEntry.TABLE_NAME, null);
        assertEquals(1, testCursor.getCount());
        testCursor.moveToFirst();
        assertEquals(resultingMinimumEntryUUID, testCursor.getString(testCursor.getColumnIndex(
                ListEntry.COLUMN.ID)));
        assertEquals(listUUID, testCursor.getString(testCursor.getColumnIndex(ListEntry.COLUMN.LIST)));
        assertEquals(productUUID, testCursor.getString(testCursor.getColumnIndex(
                ListEntry.COLUMN.PRODUCT)));
        testCursor.close();
    }

    public void testUpdateCategory() {
        String categoryUUID = UUID.randomUUID().toString();
        ContentValues categoryInitialCV = new ContentValues(2);
        categoryInitialCV.put(Category.COLUMN.ID, categoryUUID);
        categoryInitialCV.put(Category.COLUMN.NAME, "category before renaming");
        mDatabase.insert(Category.TABLE_NAME, null, categoryInitialCV);

        ContentValues categoryUpdateCV = new ContentValues(1);
        categoryUpdateCV.put(Category.COLUMN.NAME, "category after renaming");
        assertEquals(0, mCategoryProvider.update(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI,
                "category/" + UUID.randomUUID().toString()), categoryUpdateCV, null, null));
        Cursor testCursor = mDatabase.query(
                Category.TABLE_NAME,
                new String[]{
                        Category.COLUMN.NAME
                },
                Category.COLUMN.ID + " = ?",
                new String[]{
                        categoryUUID
                },
                null, null, null);
        testCursor.moveToFirst();
        assertEquals("category before renaming", testCursor.getString(
                testCursor.getColumnIndex(Category.COLUMN.NAME)));
        testCursor.close();

        assertEquals(1, mCategoryProvider.update(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI,
                "category/" + categoryUUID), categoryUpdateCV, null, null));
        testCursor = mDatabase.query(
                Category.TABLE_NAME,
                new String[]{
                        Category.COLUMN.NAME
                },
                Category.COLUMN.ID + " = ?",
                new String[]{
                        categoryUUID
                },
                null, null, null);
        testCursor.moveToFirst();
        assertEquals("category after renaming", testCursor.getString(
                testCursor.getColumnIndex(Category.COLUMN.NAME)));
        testCursor.close();
    }

    public void testUpdateList() {
        String categoryUUID = UUID.randomUUID().toString();
        ContentValues categoryCV = new ContentValues(2);
        categoryCV.put(Category.COLUMN.ID, categoryUUID);
        categoryCV.put(Category.COLUMN.NAME, "category one");
        mDatabase.insert(Category.TABLE_NAME, null, categoryCV);
        String listUUID = UUID.randomUUID().toString();
        ContentValues listInitialCV = new ContentValues(2);
        listInitialCV.put(ShoppingList.COLUMN.NAME, "list before rename");
        listInitialCV.put(ShoppingList.COLUMN.ID, listUUID);
        mDatabase.insert(ShoppingList.TABLE_NAME, null, listInitialCV);

        ContentValues listUpdateCV = new ContentValues(2);
        listUpdateCV.put(ShoppingList.COLUMN.NAME, "list after rename");
        listUpdateCV.put(ShoppingList.COLUMN.CATEGORY, categoryUUID);
        // negative: with wrong category
        assertEquals(0, mCategoryProvider.update(Uri.withAppendedPath(
                InstalistProvider.BASE_CONTENT_URI,
                "category/" + categoryUUID + "/list/" + listUUID), listUpdateCV, null, null));
        Cursor testCursor = mDatabase.query(
                ShoppingList.TABLE_NAME,
                new String[]{
                        ShoppingList.COLUMN.NAME,
                        ShoppingList.COLUMN.CATEGORY
                },
                ShoppingList.COLUMN.ID + " = ?",
                new String[]{
                        listUUID
                },
                null, null, null);
        testCursor.moveToFirst();
        assertEquals("list before rename", testCursor.getString(testCursor.getColumnIndex(
                ShoppingList.COLUMN.NAME)));
        assertNull(testCursor.getString(testCursor.getColumnIndex(ShoppingList.COLUMN.CATEGORY)));
        testCursor.close();

        // negative: with wrong list
        assertEquals(0, mCategoryProvider.update(Uri.withAppendedPath(
                InstalistProvider.BASE_CONTENT_URI,
                "category/-/list/" + UUID.randomUUID().toString()), listUpdateCV, null, null));
        testCursor = mDatabase.query(
                ShoppingList.TABLE_NAME,
                new String[]{
                        ShoppingList.COLUMN.NAME,
                        ShoppingList.COLUMN.CATEGORY
                },
                ShoppingList.COLUMN.ID + " = ?",
                new String[]{
                        listUUID
                },
                null, null, null);
        testCursor.moveToFirst();
        assertEquals("list before rename", testCursor.getString(testCursor.getColumnIndex(
                ShoppingList.COLUMN.NAME)));
        assertNull(testCursor.getString(testCursor.getColumnIndex(ShoppingList.COLUMN.CATEGORY)));
        testCursor.close();

        // positive: all right
        assertEquals(1, mCategoryProvider.update(Uri.withAppendedPath(
                InstalistProvider.BASE_CONTENT_URI,
                "category/-/list/" + listUUID), listUpdateCV, null, null));
        testCursor = mDatabase.query(
                ShoppingList.TABLE_NAME,
                new String[]{
                        ShoppingList.COLUMN.NAME,
                        ShoppingList.COLUMN.CATEGORY
                },
                ShoppingList.COLUMN.ID + " = ?",
                new String[]{
                        listUUID
                },
                null, null, null);
        testCursor.moveToFirst();
        assertEquals("list after rename", testCursor.getString(testCursor.getColumnIndex(
                ShoppingList.COLUMN.NAME)));
        assertEquals(categoryUUID, testCursor.getString(testCursor.getColumnIndex(
                ShoppingList.COLUMN.CATEGORY)));
        testCursor.close();

    }

    public void testUpdateEntry() {
        String categoryUUID = UUID.randomUUID().toString();
        ContentValues categoryCV = new ContentValues(2);
        categoryCV.put(Category.COLUMN.ID, categoryUUID);
        categoryCV.put(Category.COLUMN.NAME, "category one");
        mDatabase.insert(Category.TABLE_NAME, null, categoryCV);
        String listUUID = UUID.randomUUID().toString();
        ContentValues listCV = new ContentValues(2);
        listCV.put(ShoppingList.COLUMN.NAME, "list one");
        listCV.put(ShoppingList.COLUMN.ID, listUUID);
        listCV.put(ShoppingList.COLUMN.CATEGORY, categoryUUID);
        mDatabase.insert(ShoppingList.TABLE_NAME, null, listCV);
        String productUUID = UUID.randomUUID().toString();
        ContentValues productCV = new ContentValues(2);
        productCV.put(Product.COLUMN.ID, productUUID);
        productCV.put(Product.COLUMN.NAME, "product one");
        mDatabase.insert(Product.TABLE_NAME, null, productCV);
        String entryUUID = UUID.randomUUID().toString();
        ContentValues entryInitialCV = new ContentValues(6);
        entryInitialCV.put(ListEntry.COLUMN.ID, entryUUID);
        entryInitialCV.put(ListEntry.COLUMN.LIST, listUUID);
        entryInitialCV.put(ListEntry.COLUMN.PRODUCT, productUUID);
        entryInitialCV.put(ListEntry.COLUMN.AMOUNT, 1.0f);
        entryInitialCV.put(ListEntry.COLUMN.PRIORITY, 1.0f);
        entryInitialCV.put(ListEntry.COLUMN.STRUCK, false);
        mDatabase.insert(ListEntry.TABLE_NAME, null, entryInitialCV);

        ContentValues entryUpdateCV = new ContentValues(3);
        entryUpdateCV.put(ListEntry.COLUMN.AMOUNT, 2.0f);
        entryUpdateCV.put(ListEntry.COLUMN.PRIORITY, 2.0f);
        entryUpdateCV.put(ListEntry.COLUMN.STRUCK, true);
        // negative: with wrong category
        assertEquals(0, mCategoryProvider.update(Uri.withAppendedPath(
                InstalistProvider.BASE_CONTENT_URI,
                "category/-/list/" + listUUID + "/entry/" + entryUUID), entryUpdateCV, null, null));
        Cursor testCursor = mDatabase.query(
                ListEntry.TABLE_NAME,
                new String[]{
                        ListEntry.COLUMN.AMOUNT,
                        ListEntry.COLUMN.PRIORITY,
                        ListEntry.COLUMN.STRUCK
                },
                ListEntry.COLUMN.ID + " = ?",
                new String[]{
                        entryUUID
                },
                null, null, null);
        testCursor.moveToFirst();
        assertEquals(1.0f, testCursor.getFloat(testCursor.getColumnIndex(
                ListEntry.COLUMN.AMOUNT)), 0.001f);
        assertEquals(1.0f, testCursor.getFloat(testCursor.getColumnIndex(
                ListEntry.COLUMN.PRIORITY)), 0.001f);
        assertEquals(0, testCursor.getInt(testCursor.getColumnIndex(
                ListEntry.COLUMN.STRUCK)));
        testCursor.close();

        // negative: with wrong list
        assertEquals(0, mCategoryProvider.update(Uri.withAppendedPath(
                InstalistProvider.BASE_CONTENT_URI,
                "category/" + categoryUUID + "/list/" + UUID.randomUUID().toString() + "/entry/" +
                        entryUUID), entryUpdateCV, null, null));
        testCursor = mDatabase.query(
                ListEntry.TABLE_NAME,
                new String[]{
                        ListEntry.COLUMN.AMOUNT,
                        ListEntry.COLUMN.PRIORITY,
                        ListEntry.COLUMN.STRUCK
                },
                ListEntry.COLUMN.ID + " = ?",
                new String[]{
                        entryUUID
                },
                null, null, null);
        testCursor.moveToFirst();
        assertEquals(1.0f, testCursor.getFloat(testCursor.getColumnIndex(
                ListEntry.COLUMN.AMOUNT)), 0.001f);
        assertEquals(1.0f, testCursor.getFloat(testCursor.getColumnIndex(
                ListEntry.COLUMN.PRIORITY)), 0.001f);
        assertEquals(0, testCursor.getInt(testCursor.getColumnIndex(
                ListEntry.COLUMN.STRUCK)));
        testCursor.close();

        // negative: with wrong entry
        assertEquals(0, mCategoryProvider.update(Uri.withAppendedPath(
                InstalistProvider.BASE_CONTENT_URI,
                "category/" + categoryUUID + "/list/" + listUUID + "/entry/" +
                        UUID.randomUUID().toString()), entryUpdateCV, null, null));
        testCursor = mDatabase.query(
                ListEntry.TABLE_NAME,
                new String[]{
                        ListEntry.COLUMN.AMOUNT,
                        ListEntry.COLUMN.PRIORITY,
                        ListEntry.COLUMN.STRUCK
                },
                ListEntry.COLUMN.ID + " = ?",
                new String[]{
                        entryUUID
                },
                null, null, null);
        testCursor.moveToFirst();
        assertEquals(1.0f, testCursor.getFloat(testCursor.getColumnIndex(
                ListEntry.COLUMN.AMOUNT)), 0.001f);
        assertEquals(1.0f, testCursor.getFloat(testCursor.getColumnIndex(
                ListEntry.COLUMN.PRIORITY)), 0.001f);
        assertEquals(0, testCursor.getInt(testCursor.getColumnIndex(
                ListEntry.COLUMN.STRUCK)));
        testCursor.close();

        // positive: all right
        assertEquals(1, mCategoryProvider.update(Uri.withAppendedPath(
                        InstalistProvider.BASE_CONTENT_URI,
                        "category/" + categoryUUID + "/list/" + listUUID + "/entry/" + entryUUID),
                entryUpdateCV, null, null));
        testCursor = mDatabase.query(
                ListEntry.TABLE_NAME,
                new String[]{
                        ListEntry.COLUMN.AMOUNT,
                        ListEntry.COLUMN.PRIORITY,
                        ListEntry.COLUMN.STRUCK
                },
                ListEntry.COLUMN.ID + " = ?",
                new String[]{
                        entryUUID
                },
                null, null, null);
        testCursor.moveToFirst();
        assertEquals(2.0f, testCursor.getFloat(testCursor.getColumnIndex(
                ListEntry.COLUMN.AMOUNT)), 0.001f);
        assertEquals(2.0f, testCursor.getFloat(testCursor.getColumnIndex(
                ListEntry.COLUMN.PRIORITY)), 0.001f);
        assertEquals(1, testCursor.getInt(testCursor.getColumnIndex(
                ListEntry.COLUMN.STRUCK)));
        testCursor.close();

        // negative: with wrong content values
        ContentValues entryFalseUpdateCV = new ContentValues(2);
        entryFalseUpdateCV.put(ListEntry.COLUMN.PRODUCT, UUID.randomUUID().toString());
        entryFalseUpdateCV.put(ListEntry.COLUMN.LIST, UUID.randomUUID().toString());
        assertEquals(0, mCategoryProvider.update(Uri.withAppendedPath(
                        InstalistProvider.BASE_CONTENT_URI,
                        "category/" + categoryUUID + "/list/" + listUUID + "/entry/" + entryUUID),
                entryFalseUpdateCV, null, null));
        testCursor = mDatabase.query(
                ListEntry.TABLE_NAME,
                new String[]{
                        ListEntry.COLUMN.PRODUCT,
                        ListEntry.COLUMN.LIST
                },
                ListEntry.COLUMN.ID + " = ?",
                new String[]{
                        entryUUID
                },
                null, null, null);
        testCursor.moveToFirst();
        assertEquals(productUUID, testCursor.getString(testCursor.getColumnIndex(
                ListEntry.COLUMN.PRODUCT)));
        assertEquals(listUUID, testCursor.getString(testCursor.getColumnIndex(
                ListEntry.COLUMN.LIST)));
        testCursor.close();
    }

    public void testDeleteCategory() {
        String categoryUUID1 = UUID.randomUUID().toString();
        ContentValues categoryCV1 = new ContentValues(2);
        categoryCV1.put(Category.COLUMN.ID, categoryUUID1);
        categoryCV1.put(Category.COLUMN.NAME, "category one");
        mDatabase.insert(Category.TABLE_NAME, null, categoryCV1);
        String categoryUUID2 = UUID.randomUUID().toString();
        ContentValues categoryCV2 = new ContentValues(2);
        categoryCV2.put(Category.COLUMN.ID, categoryUUID2);
        categoryCV2.put(Category.COLUMN.NAME, "category two");
        mDatabase.insert(Category.TABLE_NAME, null, categoryCV2);

        // negative: wrong category
        assertEquals(0, mCategoryProvider.delete(
                Uri.withAppendedPath(
                        InstalistProvider.BASE_CONTENT_URI,
                        "category/" + UUID.randomUUID().toString()
                ), null, null));
        Cursor testCursor = mDatabase.query(Category.TABLE_NAME, null, null, null, null, null, null);
        assertEquals(2, testCursor.getCount());
        testCursor.moveToFirst();
        boolean cat1Complete = false;
        boolean cat2Complete = false;
        while (!testCursor.isAfterLast()) {
            if (categoryUUID1.equals(testCursor.getString(testCursor.getColumnIndex(
                    Category.COLUMN.ID))) && "category one".equals(testCursor.getString
                    (testCursor.getColumnIndex(Category.COLUMN.NAME)))) {
                cat1Complete = true;
            } else if (categoryUUID2.equals(testCursor.getString(testCursor.getColumnIndex(
                    Category.COLUMN.ID))) && "category two".equals(testCursor.getString
                    (testCursor.getColumnIndex(Category.COLUMN.NAME)))) {
                cat2Complete = true;
            }
            testCursor.moveToNext();
        }
        assertTrue(cat1Complete);
        assertTrue(cat2Complete);
        testCursor.close();

        // positive: all right
        assertEquals(1, mCategoryProvider.delete(
                Uri.withAppendedPath(
                        InstalistProvider.BASE_CONTENT_URI,
                        "category/" + categoryUUID1
                ), null, null));
        testCursor = mDatabase.query(Category.TABLE_NAME, null, null, null, null, null, null);
        assertEquals(1, testCursor.getCount());
        testCursor.moveToFirst();
        assertEquals(categoryUUID2, testCursor.getString(
                testCursor.getColumnIndex(Category.COLUMN.ID)));
        assertEquals("category two", testCursor.getString(
                testCursor.getColumnIndex(Category.COLUMN.NAME)));
        testCursor.close();
    }

    public void testDeleteList() {
        String categoryUUID = UUID.randomUUID().toString();
        ContentValues categoryCV = new ContentValues(2);
        categoryCV.put(Category.COLUMN.ID, categoryUUID);
        categoryCV.put(Category.COLUMN.NAME, "category one");
        mDatabase.insert(Category.TABLE_NAME, null, categoryCV);
        String listUUID1 = UUID.randomUUID().toString();
        ContentValues listCV1 = new ContentValues(3);
        listCV1.put(ShoppingList.COLUMN.ID, listUUID1);
        listCV1.put(ShoppingList.COLUMN.NAME, "list one");
        listCV1.put(ShoppingList.COLUMN.CATEGORY, categoryUUID);
        mDatabase.insert(ShoppingList.TABLE_NAME, null, listCV1);
        String listUUID2 = UUID.randomUUID().toString();
        ContentValues listCV2 = new ContentValues(3);
        listCV2.put(ShoppingList.COLUMN.ID, listUUID2);
        listCV2.put(ShoppingList.COLUMN.NAME, "list two");
        listCV2.put(ShoppingList.COLUMN.CATEGORY, categoryUUID);
        mDatabase.insert(ShoppingList.TABLE_NAME, null, listCV2);

        // negative: wrong category
        assertEquals(0, mCategoryProvider.delete(
                Uri.withAppendedPath(
                        InstalistProvider.BASE_CONTENT_URI,
                        "category/-/list/" + listUUID1
                ), null, null));
        Cursor testCursor = mDatabase.query(ShoppingList.TABLE_NAME, null, null, null, null, null, null);
        assertEquals(2, testCursor.getCount());
        testCursor.moveToFirst();
        boolean list1Complete = false;
        boolean list2Complete = false;
        while (!testCursor.isAfterLast()) {
            if (listUUID1.equals(testCursor.getString(testCursor.getColumnIndex(
                    ShoppingList.COLUMN.ID))) && "list one".equals(testCursor.getString
                    (testCursor.getColumnIndex(ShoppingList.COLUMN.NAME))) && categoryUUID.
                    equals(testCursor.getString(testCursor.getColumnIndex(
                            ShoppingList.COLUMN.CATEGORY)))) {
                list1Complete = true;
            } else if (listUUID2.equals(testCursor.getString(testCursor.getColumnIndex(
                    ShoppingList.COLUMN.ID))) && "list two".equals(testCursor.getString
                    (testCursor.getColumnIndex(ShoppingList.COLUMN.NAME))) && categoryUUID.
                    equals(testCursor.getString(testCursor.getColumnIndex(
                            ShoppingList.COLUMN.CATEGORY)))) {
                list2Complete = true;
            }
            testCursor.moveToNext();
        }
        assertTrue(list1Complete);
        assertTrue(list2Complete);
        testCursor.close();

        // negative: wrong list
        assertEquals(0, mCategoryProvider.delete(
                Uri.withAppendedPath(
                        InstalistProvider.BASE_CONTENT_URI,
                        "category/" + categoryUUID + "/list/" + UUID.randomUUID().toString()
                ), null, null));
        testCursor = mDatabase.query(ShoppingList.TABLE_NAME, null, null, null, null, null, null);
        assertEquals(2, testCursor.getCount());
        testCursor.moveToFirst();
        list1Complete = false;
        list2Complete = false;
        while (!testCursor.isAfterLast()) {
            if (listUUID1.equals(testCursor.getString(testCursor.getColumnIndex(
                    ShoppingList.COLUMN.ID))) && "list one".equals(testCursor.getString
                    (testCursor.getColumnIndex(ShoppingList.COLUMN.NAME))) && categoryUUID.
                    equals(testCursor.getString(testCursor.getColumnIndex(
                            ShoppingList.COLUMN.CATEGORY)))) {
                list1Complete = true;
            } else if (listUUID2.equals(testCursor.getString(testCursor.getColumnIndex(
                    ShoppingList.COLUMN.ID))) && "list two".equals(testCursor.getString
                    (testCursor.getColumnIndex(ShoppingList.COLUMN.NAME))) && categoryUUID.
                    equals(testCursor.getString(testCursor.getColumnIndex(
                            ShoppingList.COLUMN.CATEGORY)))) {
                list2Complete = true;
            }
            testCursor.moveToNext();
        }
        assertTrue(list1Complete);
        assertTrue(list2Complete);
        testCursor.close();

        // positive: all right
        assertEquals(1, mCategoryProvider.delete(
                Uri.withAppendedPath(
                        InstalistProvider.BASE_CONTENT_URI,
                        "category/" + categoryUUID + "/list/" + listUUID1
                ), null, null));
        testCursor = mDatabase.query(ShoppingList.TABLE_NAME, null, null, null, null, null, null);
        assertEquals(1, testCursor.getCount());
        testCursor.moveToFirst();
        assertEquals(listUUID2, testCursor.getString(
                testCursor.getColumnIndex(ShoppingList.COLUMN.ID)));
        assertEquals("list two", testCursor.getString(
                testCursor.getColumnIndex(ShoppingList.COLUMN.NAME)));
        testCursor.close();
    }

    public void testDeleteEntry() {
        String categoryUUID = UUID.randomUUID().toString();
        ContentValues categoryCV = new ContentValues(2);
        categoryCV.put(Category.COLUMN.ID, categoryUUID);
        categoryCV.put(Category.COLUMN.NAME, "category one");
        mDatabase.insert(Category.TABLE_NAME, null, categoryCV);
        String listUUID = UUID.randomUUID().toString();
        ContentValues listCV = new ContentValues(3);
        listCV.put(ShoppingList.COLUMN.ID, listUUID);
        listCV.put(ShoppingList.COLUMN.NAME, "list one");
        listCV.put(ShoppingList.COLUMN.CATEGORY, categoryUUID);
        mDatabase.insert(ShoppingList.TABLE_NAME, null, listCV);
        String productUUID = UUID.randomUUID().toString();
        ContentValues productCV = new ContentValues(2);
        productCV.put(Product.COLUMN.ID, productUUID);
        productCV.put(Product.COLUMN.NAME, "product one");
        mDatabase.insert(Product.TABLE_NAME, null, productCV);
        String entryUUID1 = UUID.randomUUID().toString();
        ContentValues entryCV1 = new ContentValues(3);
        entryCV1.put(ListEntry.COLUMN.ID, entryUUID1);
        entryCV1.put(ListEntry.COLUMN.LIST, listUUID);
        entryCV1.put(ListEntry.COLUMN.PRODUCT, productUUID);
        mDatabase.insert(ListEntry.TABLE_NAME, null, entryCV1);
        // Normally a linked product should only be linked once per list. For test purposes we allow
        // this also multiple times, since the test is enough hard to read.
        String entryUUID2 = UUID.randomUUID().toString();
        ContentValues entryCV2 = new ContentValues(3);
        entryCV2.put(ListEntry.COLUMN.ID, entryUUID2);
        entryCV2.put(ListEntry.COLUMN.LIST, listUUID);
        entryCV2.put(ListEntry.COLUMN.PRODUCT, productUUID);
        mDatabase.insert(ListEntry.TABLE_NAME, null, entryCV2);

        // negative: wrong category
        assertEquals(0, mCategoryProvider.delete(
                Uri.withAppendedPath(
                        InstalistProvider.BASE_CONTENT_URI,
                        "category/-/list/" + listUUID + "/entry/" + entryUUID1
                ), null, null));
        Cursor testCursor = mDatabase.query(ListEntry.TABLE_NAME, null, null, null, null, null, null);
        assertEquals(2, testCursor.getCount());
        testCursor.moveToFirst();
        boolean entry1Complete = false;
        boolean entry2Complete = false;
        while (!testCursor.isAfterLast()) {
            if (entryUUID1.equals(testCursor.getString(testCursor.getColumnIndex(
                    ListEntry.COLUMN.ID))) && listUUID.equals(testCursor.getString
                    (testCursor.getColumnIndex(ListEntry.COLUMN.LIST))) && productUUID.
                    equals(testCursor.getString(testCursor.getColumnIndex(
                            ListEntry.COLUMN.PRODUCT)))) {
                entry1Complete = true;
            } else if (entryUUID2.equals(testCursor.getString(testCursor.getColumnIndex(
                    ListEntry.COLUMN.ID))) && listUUID.equals(testCursor.getString
                    (testCursor.getColumnIndex(ListEntry.COLUMN.LIST))) && productUUID.
                    equals(testCursor.getString(testCursor.getColumnIndex(
                            ListEntry.COLUMN.PRODUCT)))) {
                entry2Complete = true;
            }
            testCursor.moveToNext();
        }
        assertTrue(entry1Complete);
        assertTrue(entry2Complete);
        testCursor.close();

        // negative: wrong list
        assertEquals(0, mCategoryProvider.delete(
                Uri.withAppendedPath(
                        InstalistProvider.BASE_CONTENT_URI,
                        "category/" + categoryUUID + "/list/" + UUID.randomUUID().toString() +
                                "/entry/" + entryUUID1
                ), null, null));
        testCursor = mDatabase.query(ListEntry.TABLE_NAME, null, null, null, null, null, null);
        assertEquals(2, testCursor.getCount());
        testCursor.moveToFirst();
        entry1Complete = false;
        entry2Complete = false;
        while (!testCursor.isAfterLast()) {
            if (entryUUID1.equals(testCursor.getString(testCursor.getColumnIndex(
                    ListEntry.COLUMN.ID))) && listUUID.equals(testCursor.getString
                    (testCursor.getColumnIndex(ListEntry.COLUMN.LIST))) && productUUID.
                    equals(testCursor.getString(testCursor.getColumnIndex(
                            ListEntry.COLUMN.PRODUCT)))) {
                entry1Complete = true;
            } else if (entryUUID2.equals(testCursor.getString(testCursor.getColumnIndex(
                    ListEntry.COLUMN.ID))) && listUUID.equals(testCursor.getString
                    (testCursor.getColumnIndex(ListEntry.COLUMN.LIST))) && productUUID.
                    equals(testCursor.getString(testCursor.getColumnIndex(
                            ListEntry.COLUMN.PRODUCT)))) {
                entry2Complete = true;
            }
            testCursor.moveToNext();
        }
        assertTrue(entry1Complete);
        assertTrue(entry2Complete);
        testCursor.close();

        // negative: wrong entry
        assertEquals(0, mCategoryProvider.delete(
                Uri.withAppendedPath(
                        InstalistProvider.BASE_CONTENT_URI,
                        "category/" + categoryUUID + "/list/" + listUUID + "/entry/" + UUID.
                                randomUUID().toString()
                ), null, null));
        testCursor = mDatabase.query(ListEntry.TABLE_NAME, null, null, null, null, null, null);
        assertEquals(2, testCursor.getCount());
        testCursor.moveToFirst();
        entry1Complete = false;
        entry2Complete = false;
        while (!testCursor.isAfterLast()) {
            if (entryUUID1.equals(testCursor.getString(testCursor.getColumnIndex(
                    ListEntry.COLUMN.ID))) && listUUID.equals(testCursor.getString
                    (testCursor.getColumnIndex(ListEntry.COLUMN.LIST))) && productUUID.
                    equals(testCursor.getString(testCursor.getColumnIndex(
                            ListEntry.COLUMN.PRODUCT)))) {
                entry1Complete = true;
            } else if (entryUUID2.equals(testCursor.getString(testCursor.getColumnIndex(
                    ListEntry.COLUMN.ID))) && listUUID.equals(testCursor.getString
                    (testCursor.getColumnIndex(ListEntry.COLUMN.LIST))) && productUUID.
                    equals(testCursor.getString(testCursor.getColumnIndex(
                            ListEntry.COLUMN.PRODUCT)))) {
                entry2Complete = true;
            }
            testCursor.moveToNext();
        }
        assertTrue(entry1Complete);
        assertTrue(entry2Complete);
        testCursor.close();

        // positive: all right
        assertEquals(1, mCategoryProvider.delete(
                Uri.withAppendedPath(
                        InstalistProvider.BASE_CONTENT_URI,
                        "category/" + categoryUUID + "/list/" + listUUID + "/entry/" + entryUUID1
                ), null, null));
        testCursor = mDatabase.query(ListEntry.TABLE_NAME, null, null, null, null, null, null);
        assertEquals(1, testCursor.getCount());
        testCursor.moveToFirst();
        assertEquals(entryUUID2, testCursor.getString(
                testCursor.getColumnIndex(ListEntry.COLUMN.ID)));
        assertEquals(productUUID, testCursor.getString(
                testCursor.getColumnIndex(ListEntry.COLUMN.PRODUCT)));
        assertEquals(listUUID, testCursor.getString(
                testCursor.getColumnIndex(ListEntry.COLUMN.LIST)));
        testCursor.close();
    }
}