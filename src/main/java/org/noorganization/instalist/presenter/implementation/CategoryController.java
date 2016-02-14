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
import org.noorganization.instalist.presenter.event.CategoryChangedMessage;
import org.noorganization.instalist.presenter.event.Change;
import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.provider.InstalistProvider;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class CategoryController implements ICategoryController {
    private static CategoryController mInstance;

    private EventBus mBus;
    private Context  mContext;
    private ContentResolver mResolver;

    private CategoryController(@NonNull Context _context) {
        mContext = _context;
        mBus = EventBus.getDefault();
        mResolver = mContext.getContentResolver();
    }

    static CategoryController getInstance(@NonNull Context _context) {
        if (mInstance == null) {
            mInstance = new CategoryController(_context);
        }
        return mInstance;
    }

    @Override
    public Category createCategory(String _name) {
        if (_name == null || _name.length() == 0 || nameUsed(_name, null)) {
            return null;
        }

        ContentValues newCatCV = new ContentValues(1);
        newCatCV.put(Category.COLUMN.NAME, _name);
        Uri newCreatedCat = mContext.getContentResolver().insert(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category"),
                newCatCV);
        if (newCreatedCat == null) {
            return null;
        }

        Category rtn = new Category(newCreatedCat.getLastPathSegment(), _name);

        mBus.post(new CategoryChangedMessage(Change.CREATED, rtn));

        return rtn;
    }

    @Override
    public List<Category> getAllCategories() {
        Cursor catIds = mResolver.query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category"),
                new String[]{Category.COLUMN.ID},
                null, null, null);
        if (catIds == null) {
            Log.e(getClass().getCanonicalName(), "Querying for categories failed. " +
                    "Returning error.");
            return null;
        }
        ArrayList<Category> rtn = new ArrayList<>(catIds.getCount());
        catIds.moveToFirst();
        while (!catIds.isAfterLast()) {
            rtn.add(getCategoryByID(catIds.getString(catIds.getColumnIndex(Category.COLUMN.ID))));
            catIds.moveToNext();
        }
        catIds.close();

        // add default list
        rtn.add(new Category(null, "Default"));
        return rtn;
    }

    @Override
    public Category getCategoryByID(@NonNull String _uuid) {
        // Annotation does not always work. So check again.
        if (_uuid == null) {
            return null;
        }

        Cursor resultCursor = mContext.getContentResolver().query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category"),
                new String[]{Category.COLUMN.ID, Category.COLUMN.NAME},
                Category.COLUMN.ID + " = ?",
                new String[]{_uuid},
                null);
        if (resultCursor == null) {
            Log.e(getClass().getCanonicalName(), "Query result was null. Returning no result.");
            return null;
        }
        if (resultCursor.getCount() == 0) {
            resultCursor.close();
            return null;
        }

        resultCursor.moveToFirst();
        Category rtn = new Category();
        rtn.mUUID = _uuid;
        rtn.mName = resultCursor.getString(resultCursor.getColumnIndex(
                Category.COLUMN.NAME));
        resultCursor.close();
        return rtn;
    }

    @Override
    public int getCategoryCount() {
        Cursor catIds = mResolver.query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category"),
                new String[]{Category.COLUMN.ID},
                null, null, null);
        if (catIds == null) {
            Log.e(getClass().getCanonicalName(), "Querying for categories to count them failed. " +
                    "Return 0.");
            return 0;
        }
        int rtn = catIds.getCount();
        catIds.close();
        return rtn;
    }

    @Override
    public Category renameCategory(Category _toRename, String _newName) {
        if (_toRename == null) {
            return null;
        }

        Category rtn = getCategoryByID(_toRename.mUUID);
        if (rtn == null) {
            return null;
        }

        if (_newName != null && _newName.length() > 0 && !nameUsed(_newName, rtn.mUUID)) {
            ContentValues updateCV = new ContentValues(1);
            updateCV.put(Category.COLUMN.NAME, _newName);
            if (mContext.getContentResolver().update(
                    Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category/" + rtn.mUUID),
                    updateCV,
                    null, null) == 1) {
                rtn.mName = _newName;
            }
        }

        mBus.post(new CategoryChangedMessage(Change.CHANGED, rtn));

        return rtn;
    }

    @Override
    public void removeCategory(Category _toRemove) {
        if (_toRemove == null) {
            return;
        }

        Category savedCategory = getCategoryByID(_toRemove.mUUID);
        if (savedCategory != null) {
            IListController listController = ControllerFactory.getListController(mContext);
            for (ShoppingList listToUnlink : listController.getListsByCategory(savedCategory)) {
                if (listController.moveToCategory(listToUnlink, null).equals(listToUnlink)) {
                    Log.e("CategoryController", "List could not be moved");
                }
            }

            if (mContext.getContentResolver().delete(
                    Uri.withAppendedPath(
                            InstalistProvider.BASE_CONTENT_URI,
                            "category/"+_toRemove.mUUID),
                    null,
                    null) == 1) {

                mBus.post(new CategoryChangedMessage(Change.DELETED, _toRemove));
            }
        }
    }

    private boolean nameUsed(String _search, String _ignoreId) {
        Cursor catsToCheck;
        if (_ignoreId != null) {
            catsToCheck = mContext.getContentResolver().query(
                    Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category"),
                    null,
                    Category.COLUMN.ID + " != ? AND " +
                            Category.COLUMN.NAME + " = ?",
                    new String[]{
                            _ignoreId,
                            _search
                    },
                    null);
        } else {
            catsToCheck = mContext.getContentResolver().query(
                    Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "category"),
                    null,
                    Category.COLUMN.NAME + " = ?",
                    new String[]{ _search },
                    null);
        }
        if (catsToCheck == null) {
            Log.e(getClass().getCanonicalName(), "Query for searching name duplicates nulled. " +
                    "Returning true (= there is a duplicate).");
            return true;
        }
        int count = catsToCheck.getCount();
        catsToCheck.close();

        return (count != 0);
    }
}
