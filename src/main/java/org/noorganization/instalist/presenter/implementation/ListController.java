package org.noorganization.instalist.presenter.implementation;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import org.noorganization.instalist.presenter.ICategoryController;
import org.noorganization.instalist.presenter.IListController;
import org.noorganization.instalist.presenter.IProductController;
import org.noorganization.instalist.presenter.event.Change;
import org.noorganization.instalist.presenter.event.ListChangedMessage;
import org.noorganization.instalist.presenter.event.ListItemChangedMessage;
import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.model.ListEntry;
import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.provider.InstalistProvider;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


/**
 * Implementation of {@link org.noorganization.instalist.presenter.IListController} as
 * singleton. Please retrieve your instance per {@link ListController#getInstance(Context _context)}.
 */
public class ListController implements IListController {

    private static String LOG_TAG = ListController.class.getName();
    private static ListController mInstance;

    private EventBus mBus;
    private WeakReference<Context> mContext;
    private IProductController mProductController;
    private ICategoryController mCategoryController;
    private ContentResolver mResolver;

    private ListController(Context _context) {
        mBus = EventBus.getDefault();
        mContext = new WeakReference<>(_context);
        mProductController = ControllerFactory.getProductController(_context);
        mCategoryController = ControllerFactory.getCategoryController(_context);
        mResolver = mContext.get().getContentResolver();
    }

    public static ListController getInstance(Context _context) {
        if (mInstance == null) {
            mInstance = new ListController(_context);
        }

        return mInstance;
    }


    // region Public methods

    @Override
    public ListEntry addOrChangeItem(ShoppingList _list, Product _product, float _amount) {
        return addOrChangeItem(_list, _product, _amount, false, 0, false);
    }

    @Override
    public ListEntry addOrChangeItem(ShoppingList _list, Product _product, float _amount, int _prio) {
        return addOrChangeItem(_list, _product, _amount, true, _prio, false);
    }

    @Override
    public ListEntry addOrChangeItem(ShoppingList _list, Product _product, float _amount, boolean _addAmount) {
        return addOrChangeItem(_list, _product, _amount, false, 0, _addAmount);
    }

    @Override
    public ListEntry addOrChangeItem(ShoppingList _list, Product _product, float _amount, int _prio,
                                     boolean _addAmount) {
        return addOrChangeItem(_list, _product, _amount, true, _prio, true);
    }

    @Override
    public List<ShoppingList> getAllLists() {
        Cursor listIDs = mResolver.query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "list"),
                new String[]{ShoppingList.COLUMN.ID}, null, null, null);
        if (listIDs == null) {
            Log.e(getClass().getCanonicalName(), "Got null-cursor while retrieving all lists. " +
                    "Returning null for error.");
            return null;
        }
        ArrayList<ShoppingList> rtn = new ArrayList<>(listIDs.getCount());
        listIDs.moveToFirst();
        while (!listIDs.isAfterLast()) {
            try {
                rtn.add(getListById(listIDs.getString(listIDs.getColumnIndex(ShoppingList.COLUMN.ID))));
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, "Illegal arguments passed in.");
            }
            listIDs.moveToNext();
        }
        listIDs.close();
        return rtn;
    }

    @Override
    public ListEntry getEntryById(@NonNull String _UUID) {
        if (_UUID == null) {
            // This check is needed because NonNull just works at compile time.
            return null;
        }
        Cursor entryCursor = mResolver.query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "entry"),
                ListEntry.COLUMN.ALL_COLUMNS,
                ListEntry.COLUMN.ID + " = ?",
                new String[]{_UUID},
                null);
        if (entryCursor == null) {
            Log.e(getClass().getCanonicalName(), "Searching ListEntry by UUID resulted null. " +
                    "Returning no ListEntry.");
            return null;
        }
        if (entryCursor.getCount() == 0) {
            entryCursor.close();
            return null;
        }
        entryCursor.moveToFirst();
        IProductController productController = ControllerFactory.getProductController(mContext.get());
        String listUUID = entryCursor.getString(entryCursor.getColumnIndex(ListEntry.COLUMN.LIST));
        String prodUUID = entryCursor.getString(entryCursor.getColumnIndex(ListEntry.COLUMN.PRODUCT));
        ListEntry rtn = new ListEntry(
                _UUID,
                getListById(listUUID),
                productController.findById(prodUUID),
                entryCursor.getFloat(entryCursor.getColumnIndex(ListEntry.COLUMN.AMOUNT)),
                entryCursor.getInt(entryCursor.getColumnIndex(ListEntry.COLUMN.STRUCK)) != 0,
                entryCursor.getInt(entryCursor.getColumnIndex(ListEntry.COLUMN.PRIORITY)));
        entryCursor.close();
        return rtn;
    }


    @Override
    public ListEntry getEntryByListAndProduct(@NonNull ShoppingList _list, @NonNull Product _product) {
        if (_list == null || _list.mUUID == null || _product == null || _product.mUUID == null) {
            // This check is needed because @NonNull does not work at runtime.
            return null;
        }
        Cursor entrySearch = mResolver.query(
                Uri.withAppendedPath(
                        InstalistProvider.BASE_CONTENT_URI,
                        _list.getUriPath() + "/entry"),
                new String[]{ListEntry.COLUMN.ID},
                ListEntry.COLUMN.PRODUCT + " = ?",
                new String[]{_product.mUUID},
                null);
        if (entrySearch == null) {
            Log.e(getClass().getCanonicalName(), "Search for ListEntry by product failed. " +
                    "Returning no found entry.");
            return null;
        } else if (entrySearch.getCount() == 0) {
            return null;
        }
        entrySearch.moveToFirst();
        ListEntry listEntry = getEntryById(entrySearch.getString(entrySearch.getColumnIndex(
                ListEntry.COLUMN.ID)));
        entrySearch.close();
        return listEntry;
    }

    @Override
    public int getEntryCount(@NonNull ShoppingList _list) {
        Cursor entryIDs = mResolver.query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, _list.getUriPath() + "/entry"),
                new String[]{ListEntry.COLUMN.ID},
                null, null, null);
        if (entryIDs == null) {
            Log.e(getClass().getCanonicalName(), "Retrieving ListEntry-IDs for counting them failed." +
                    "Returning no entries.");
            return 0;
        }
        int rtn = entryIDs.getCount();
        entryIDs.close();
        return rtn;
    }

    @Override
    public ShoppingList getListById(@NonNull String _UUID) {
        // This check is needed to make sure null-uuid will return null. @NonNull is just like a
        // warning.
        if (_UUID == null) {
            return null;
        }
        Cursor entryCursor = mContext.get().getContentResolver().query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "list"),
                ShoppingList.COLUMN.ALL_COLUMNS,
                ShoppingList.COLUMN.ID + " = ?",
                new String[]{_UUID},
                null);
        if (entryCursor == null) {
            Log.e(getClass().getCanonicalName(), "Searching ListEntry by UUID resulted null. " +
                    "Returning no ListEntry.");
            return null;
        }
        if (entryCursor.getCount() == 0) {
            entryCursor.close();
            return null;
        }
        entryCursor.moveToFirst();
        ShoppingList rtn = new ShoppingList();
        rtn.mUUID = entryCursor.getString(entryCursor.getColumnIndex(ShoppingList.COLUMN.ID));
        rtn.mName = entryCursor.getString(entryCursor.getColumnIndex(ShoppingList.COLUMN.NAME));
        if (entryCursor.isNull(entryCursor.getColumnIndex(ShoppingList.COLUMN.CATEGORY))) {
            rtn.mCategory = null;
        } else {
            rtn.mCategory = ControllerFactory.getCategoryController(mContext.get()).getCategoryByID(
                    entryCursor.getString(entryCursor.getColumnIndex(ShoppingList.COLUMN.CATEGORY)));
        }
        entryCursor.close();
        return rtn;
    }

    @Override
    public List<ShoppingList> getListsByCategory(Category _category) {
        if (_category != null) {
            Category savedCategory = mCategoryController.getCategoryByID(_category.mUUID);
            if (savedCategory == null) {
                return null;
            }
        }
        Cursor ListsInCat = mResolver.query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category/" +
                        (_category != null ? _category.mUUID : "-") + "/list"),
                new String[]{ShoppingList.COLUMN.ID}, null, null, null);
        if (ListsInCat == null) {
            Log.e(getClass().getCanonicalName(), "Got null-cursor while retrieving lists in " +
                    "category. Returning null for error.");
            return null;
        }
        ArrayList<ShoppingList> rtn = new ArrayList<>(ListsInCat.getCount());
        ListsInCat.moveToFirst();
        while (!ListsInCat.isAfterLast()) {
            String listId = ListsInCat.getString(ListsInCat.getColumnIndex(ShoppingList.COLUMN.ID));
            rtn.add(getListById(listId));
            ListsInCat.moveToNext();
        }
        ListsInCat.close();
        return rtn;
    }

    @Override
    public void strikeAllItems(ShoppingList _list) {
        if (_list == null || _list.mUUID == null) {
            return;
        }
        Cursor itemsToStrike = mResolver.query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI,
                        _list.toUri(InstalistProvider.BASE_CONTENT_URI).getPath() + "/entry"),
                new String[]{ListEntry.COLUMN.ID},
                null, null, null);
        if (itemsToStrike != null) {
            itemsToStrike.moveToFirst();
            ContentValues strikeCV = new ContentValues(1);
            strikeCV.put(ListEntry.COLUMN.STRUCK, true);
            String prefixPath = _list.toUri(InstalistProvider.BASE_CONTENT_URI).getPath() + "/entry/";
            while (!itemsToStrike.isAfterLast()) {
                String entryUUID = itemsToStrike.getString(itemsToStrike.getColumnIndex(
                        ListEntry.COLUMN.ID));
                if (mResolver.update(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI,
                        prefixPath + entryUUID), strikeCV, null, null) == 1) {
                    mBus.post(new ListItemChangedMessage(Change.CHANGED, getEntryById(entryUUID)));
                }
                itemsToStrike.moveToNext();
            }
            itemsToStrike.close();
        }
    }

    @Override
    public void unstrikeAllItems(ShoppingList _list) {
        if (_list == null || _list.mUUID == null) {
            return;
        }
        Cursor itemsToStrike = mResolver.query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI,
                        _list.toUri(InstalistProvider.BASE_CONTENT_URI).getPath() + "/entry"),
                new String[]{ListEntry.COLUMN.ID},
                null, null, null);
        if (itemsToStrike != null) {
            itemsToStrike.moveToFirst();
            ContentValues strikeCV = new ContentValues(1);
            strikeCV.put(ListEntry.COLUMN.STRUCK, false);
            String prefixPath = _list.toUri(InstalistProvider.BASE_CONTENT_URI).getPath() + "/entry/";
            while (!itemsToStrike.isAfterLast()) {
                String entryUUID = itemsToStrike.getString(itemsToStrike.getColumnIndex(
                        ListEntry.COLUMN.ID));
                if (mResolver.update(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI,
                        prefixPath + entryUUID), strikeCV, null, null) == 1) {
                    mBus.post(new ListItemChangedMessage(Change.CHANGED, getEntryById(entryUUID)));
                }
                itemsToStrike.moveToNext();
            }
            itemsToStrike.close();
        }
    }

    @Override
    public ListEntry strikeItem(ShoppingList _list, Product _product) {
        if (_list == null || _product == null) {
            return null;
        }

        ListEntry toChange = getEntryByListAndProduct(_list, _product);
        return strikeItem(toChange, false);
    }

    @Override
    public ListEntry unstrikeItem(ShoppingList _list, Product _product) {
        if (_list == null || _product == null) {
            return null;
        }

        ListEntry toChange = getEntryByListAndProduct(_list, _product);
        return unstrikeItem(toChange, false);
    }

    @Override
    public ListEntry strikeItem(ListEntry _item) {
        return strikeItem(_item, true);
    }

    @Override
    public ListEntry unstrikeItem(ListEntry _item) {
        return unstrikeItem(_item, true);
    }

    @Override
    public boolean removeItem(ShoppingList _list, Product _product) {
        if (_list == null || _product == null) {
            return false;
        }

        ListEntry toDelete = getEntryByListAndProduct(_list, _product);
        return removeItem(toDelete);
    }

    @Override
    public boolean removeItem(ListEntry _item) {
        if (_item == null) {
            return false;
        }

        int deletedEntries = mResolver.delete(_item.toUri(InstalistProvider.BASE_CONTENT_URI), null,
                null);
        if (deletedEntries > 0) {
            mBus.post(new ListItemChangedMessage(Change.DELETED, _item));
            return true;
        }
        return false;
    }

    @Override
    public ListEntry setItemPriority(ListEntry _item, int _newPrio) {
        if (_item == null) {
            return null;
        }

        ListEntry savedEntry = getEntryById(_item.mUUID);
        if (savedEntry == null) {
            return null;
        }
        ContentValues entryUpdateCV = new ContentValues(1);
        entryUpdateCV.put(ListEntry.COLUMN.PRIORITY, _newPrio);
        int changedEntries = mResolver.update(savedEntry.toUri(InstalistProvider.BASE_CONTENT_URI),
                entryUpdateCV, null, null);
        savedEntry.mPriority = _newPrio;
        if (changedEntries > 0) {
            mBus.post(new ListItemChangedMessage(Change.CHANGED, savedEntry));
        }

        return savedEntry;
    }

    @Override
    public ShoppingList addList(String _name) {
        return addList(_name, null);
    }

    @Override
    public ShoppingList addList(String _name, Category _category) {
        if (_name == null || _name.length() == 0 || existsListName(_name)) {
            return null;
        }

        ContentValues newListCV = new ContentValues(2);
        newListCV.put(ShoppingList.COLUMN.NAME, _name);

        String insertUri = InstalistProvider.BASE_CONTENT_URI.toString().concat("/category/").concat("%s").concat("/list");
        if (_category == null) {
            // no category is selected, use default one
            newListCV.putNull(ShoppingList.COLUMN.CATEGORY);
            // set the category to the default uuid
            insertUri = String.format(insertUri, "-");
        } else {
            // category is set by uuid
            newListCV.put(ShoppingList.COLUMN.CATEGORY, _category.mUUID);
            insertUri = String.format(insertUri, _category.mUUID);
        }

        // add list to db
        // TODO this should do it if I understand your intention correct
        Uri createdList = mResolver.insert(Uri.parse(insertUri), newListCV);

        if (createdList == null) {
            return null;
        }

        ShoppingList shoppingList = new ShoppingList();
        shoppingList.mUUID = createdList.getLastPathSegment();
        shoppingList.mCategory = _category;
        shoppingList.mName = _name;
        mBus.post(new ListChangedMessage(Change.CREATED, shoppingList));

        return shoppingList;
    }

    @Override
    public boolean removeList(ShoppingList _list) {
        if (_list == null) {
            return false;
        }

        Uri listUri = _list.toUri(InstalistProvider.BASE_CONTENT_URI);
        if (listUri == null) {
            return false;
        }
        int deletedListCount = mResolver.delete(listUri, null, null);

        if (deletedListCount > 0) {
            mBus.post(new ListChangedMessage(Change.DELETED, _list));
            return true;
        }

        return false;
    }

    @Override
    public ShoppingList renameList(ShoppingList _list, String _newName) {
        if (_list == null) {
            return null;
        }

        ShoppingList savedList = getListById(_list.mUUID);
        if (savedList == null || _newName == null || _newName.length() == 0 || existsListName(_newName)) {
            return savedList;
        }

        ContentValues updateCV = new ContentValues(1);
        updateCV.put(ShoppingList.COLUMN.NAME, _newName);
        int affectedRows = mResolver.update(_list.toUri(InstalistProvider.BASE_CONTENT_URI),
                updateCV, null, null);

        if (affectedRows > 0) {
            savedList.mName = _newName;
            mBus.post(new ListChangedMessage(Change.CHANGED, _list));
        }

        return savedList;
    }

    @Override
    public ShoppingList moveToCategory(ShoppingList _list, Category _category) {
        if (_list == null) {
            return null;
        }

        ShoppingList savedList = getListById(_list.mUUID);
        if (savedList == null) {
            return null;
        }

        Cursor categoryCursor;
        Category category;

        ContentValues toUpdate = new ContentValues(1);
        Category savedCategory;
        if (_category != null) {
            ICategoryController categoryController = ControllerFactory.getCategoryController(mContext.get());
            savedCategory = categoryController.getCategoryByID(_category.mUUID);
            if (savedCategory == null) {
                return savedList;
            }
            toUpdate.put(ShoppingList.COLUMN.CATEGORY, savedCategory.mUUID);
        } else {
            toUpdate.putNull(ShoppingList.COLUMN.CATEGORY);
            savedCategory = null;
        }

        int affectedRows = mResolver.update(savedList.toUri(InstalistProvider.BASE_CONTENT_URI),
                toUpdate, null, null);
        if (affectedRows > 0) {
            savedList.mCategory = savedCategory;
            mBus.post(new ListChangedMessage(Change.CHANGED, savedList));
        }

        return savedList;
    }

    @Override
    public List<ListEntry> listAllListEntries(String _shoppingListUUID, String _categoryUUID) {
        Cursor listCursor = mResolver.query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, String.format("category/%s/list/%s/entry", _categoryUUID == null ? "-" : _categoryUUID, _shoppingListUUID)),
                ListEntry.COLUMN.ALL_COLUMNS,
                null,
                null,
                null);

        if (listCursor == null) {
            return null;
        }

        List<ListEntry> listEntries = new ArrayList<>(listCursor.getCount());
        if (listCursor.getCount() == 0) {
            listCursor.close();
            return listEntries;
        }

        listCursor.moveToFirst();
        do {
            listEntries.add(parseListEntryFromCursor(listCursor));
        } while (listCursor.moveToNext());
        listCursor.close();

        return listEntries;
    }


    //endregion public methods

    //region Private methods
    private ListEntry parseListEntryFromCursor(Cursor entryCursor) {
        ListEntry rtn = new ListEntry();
        rtn.mUUID = entryCursor.getString(entryCursor.getColumnIndex(ListEntry.COLUMN.ID));
        rtn.mList = getListById(entryCursor.getString(entryCursor.getColumnIndex(
                ListEntry.COLUMN.LIST)));
        rtn.mProduct = mProductController.findById(entryCursor.getString(
                entryCursor.getColumnIndex(ListEntry.COLUMN.PRODUCT)));
        rtn.mAmount = entryCursor.getFloat(entryCursor.getColumnIndex(ListEntry.COLUMN.AMOUNT));
        rtn.mPriority = entryCursor.getInt(entryCursor.getColumnIndex(ListEntry.COLUMN.PRIORITY));
        rtn.mStruck = (entryCursor.getInt(entryCursor.getColumnIndex(ListEntry.COLUMN.STRUCK)) != 0);
        return rtn;
    }

    /**
     * Checks if an list with the given name already exists in the database.
     *
     * @param _name the name to be checked.
     * @return true if there is an exisiting one else false.
     */
    private boolean existsListName(String _name) {
        Uri requestUri = Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "list");
        Cursor listCursor = mResolver.query(requestUri, new String[]{ShoppingList.COLUMN.NAME}, ShoppingList.COLUMN.NAME + "= ?", new String[]{_name}, null);

        if (listCursor == null) {
            Log.e(LOG_TAG, "Internal failure while querying a list name in method existsListName(String _name).");
            // return true just to prevent errors that can result from this.
            return true;
        }
        boolean rtn = listCursor.getCount() > 0;
        listCursor.close();
        return rtn;
    }

    /**
     * Adds or updates a Listentry to a given {@link ShoppingList}.
     * // TODO: further description needed
     *
     * @param _list      the list where the item should be added or updated.
     * @param _product   the product to insert/update.
     * @param _amount    the new amount of the product in the given list.
     * @param _prioUsed  flag that describes if the prio is used.
     * @param _prio      the prio that will be assigned.
     * @param _addAmount the standard amount to add when hitting the inc/dec buttons.
     * @return the new listentry or null if some failure happened.
     */
    private ListEntry addOrChangeItem(ShoppingList _list, Product _product, float _amount,
                                      boolean _prioUsed, int _prio, boolean _addAmount) {
        if (_list == null || _product == null) {
            return null;
        }

        ShoppingList savedList = getListById(_list.mUUID);
        Product savedProduct = mProductController.findById(_product.mUUID);
        if (savedList == null || savedProduct == null) {
            return null;
        }

        ListEntry savedEntry = getEntryByListAndProduct(savedList, savedProduct);
        float previousAmount = (savedEntry == null ? 0.0f :
                (_addAmount ? savedEntry.mAmount : 0.0f));
        if (previousAmount + _amount <= 0.0f) {
            return savedEntry;
        }

        if (savedEntry != null) {
            ContentValues updateCV = new ContentValues(2);
            updateCV.put(ListEntry.COLUMN.AMOUNT, previousAmount + _amount);
            if (_prioUsed) {
                updateCV.put(ListEntry.COLUMN.PRIORITY, _prio);
            }
            if (mResolver.update(savedEntry.toUri(InstalistProvider.BASE_CONTENT_URI), updateCV,
                    null, null) > 0) {
                savedEntry.mAmount = previousAmount + _amount;
                if (_prioUsed) {
                    savedEntry.mPriority = _prio;
                }
                mBus.post(new ListItemChangedMessage(Change.CHANGED, savedEntry));
            }
        } else {
            ContentValues newEntryCV = new ContentValues(5);
            newEntryCV.put(ListEntry.COLUMN.LIST, savedList.mUUID);
            newEntryCV.put(ListEntry.COLUMN.PRODUCT, savedProduct.mUUID);
            newEntryCV.put(ListEntry.COLUMN.AMOUNT, _amount);
            if (_prioUsed) {
                newEntryCV.put(ListEntry.COLUMN.PRIORITY, _prio);
            }
            newEntryCV.put(ListEntry.COLUMN.STRUCK, false);

            Uri createdEntry = mResolver.insert(
                    Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI,
                            savedList.getUriPath() + "/entry"), newEntryCV);
            if (createdEntry != null) {
                savedEntry = new ListEntry(createdEntry.getLastPathSegment(), savedList,
                        savedProduct, _amount, false, (_prioUsed ? _prio :
                        ListEntry.DEFAULTS.PRIORITY));
                mBus.post(new ListItemChangedMessage(Change.CREATED, savedEntry));
            }
        }
        return savedEntry;
    }

    private ListEntry unstrikeItem(ListEntry _toChange, boolean _reload) {
        return updateStruckItem(_toChange, _reload, false);
    }

    private ListEntry updateStruckItem(ListEntry _toChange, boolean _reload, boolean _struck) {
        if (_toChange == null) {
            return null;
        }

        ContentValues entryUpdateCV = new ContentValues(1);
        entryUpdateCV.put(ListEntry.COLUMN.STRUCK, _struck);
        int chagedItems = mResolver.update(_toChange.toUri(InstalistProvider.BASE_CONTENT_URI),
                entryUpdateCV,
                null, null);
        ListEntry rtn;
        if (_reload) {
            rtn = getEntryById(_toChange.mUUID);
        } else {
            _toChange.mStruck = _struck;
            rtn = _toChange;
        }
        if (chagedItems > 0) {
            mBus.post(new ListItemChangedMessage(Change.CHANGED, rtn));
        }
        return rtn;
    }

    private ListEntry strikeItem(ListEntry _item, boolean _reload) {
        return updateStruckItem(_item, _reload, true);
    }

//endregion Private methods

}
