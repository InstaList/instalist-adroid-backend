package org.noorganization.instalist.presenter.implementation;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import org.noorganization.instalist.presenter.IProductController;
import org.noorganization.instalist.presenter.IUnitController;
import org.noorganization.instalist.presenter.event.Change;
import org.noorganization.instalist.presenter.event.UnitChangedMessage;
import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.Unit;
import org.noorganization.instalist.provider.internal.ProductProvider;
import org.noorganization.instalist.provider.internal.UnitProvider;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class UnitController implements IUnitController {

    private static String LOG_TAG = UnitController.class.getName();
    private static UnitController mInstance;

    private EventBus mBus;
    private Context mContext;
    private ContentResolver mResolver;

    private UnitController(Context _context) {
        mBus = EventBus.getDefault();
        mContext = _context;
        mResolver = mContext.getContentResolver();
    }

    static UnitController getInstance(Context _context) {
        if (mInstance == null) {
            mInstance = new UnitController(_context);
        }
        return mInstance;
    }

    @Override
    public Unit createUnit(String _name) {

        if (_name == null ||
                findByName(_name) != null) {
            return null;
        }

        Unit newUnit = new Unit();
        newUnit.mName = _name;

        Uri unitUri = mResolver.insert(Uri.parse(UnitProvider.MULTIPLE_UNIT_CONTENT_URI), newUnit.toContentValues());

        if (unitUri == null) {
            return null;
        }
        newUnit.mUUID = unitUri.getLastPathSegment();
        mBus.post(new UnitChangedMessage(Change.CREATED, newUnit));

        return newUnit;
    }

    @Override
    public Unit findById(@NonNull String _uuid) {
        Cursor unitCursor = mResolver.query(
                Uri.parse(UnitProvider.MULTIPLE_UNIT_CONTENT_URI),
                Unit.COLUMN.ALL_COLUMNS,
                Unit.COLUMN.ID + " = ?",
                new String[]{_uuid},
                null);
        if (unitCursor == null) {
            Log.e(getClass().getCanonicalName(), "Searching for Product by UUID failed with null " +
                    "instead of Cursor. Returning no Product.");
            return null;
        } else if (unitCursor.getCount() == 0) {
            unitCursor.close();
            return null;
        }
        unitCursor.moveToFirst();
        Unit rtn = new Unit();
        rtn.mUUID = _uuid;
        rtn.mName = unitCursor.getString(unitCursor.getColumnIndex(Unit.COLUMN.NAME));
        unitCursor.close();
        return rtn;
    }

    @Override
    public Unit findByName(String _name) {
        Cursor unitCursor = mResolver.query(
                Uri.parse(UnitProvider.MULTIPLE_UNIT_CONTENT_URI),
                Unit.COLUMN.ALL_COLUMNS,
                Unit.COLUMN.NAME + " = ?",
                new String[]{_name},
                null);
        if (unitCursor == null) {
            Log.e(getClass().getCanonicalName(), "Searching for Product by UUID failed with null " +
                    "instead of Cursor. Returning no Product.");
            return null;
        } else if (unitCursor.getCount() == 0) {
            unitCursor.close();
            return null;
        }
        unitCursor.moveToFirst();
        Unit rtn = new Unit();
        rtn.mUUID = unitCursor.getString(unitCursor.getColumnIndex(Unit.COLUMN.ID));
        rtn.mName = unitCursor.getString(unitCursor.getColumnIndex(Unit.COLUMN.NAME));
        unitCursor.close();
        return rtn;
    }

    @Override
    public Unit renameUnit(Unit _unit, String _newName) {
        if (_unit == null) {
            return null;
        }

        Unit toChange = findById(_unit.mUUID);
        if (toChange == null || _newName == null) {
            return toChange;
        }

        Cursor cursor = mResolver.query(Uri.parse(UnitProvider.MULTIPLE_UNIT_CONTENT_URI),
                Unit.COLUMN.ALL_COLUMNS, Unit.COLUMN.NAME + "=? AND " + Unit.COLUMN.ID + " <> ?",
                new String[]{_newName, _unit.mUUID},
                null);

        if (cursor == null) {
            return null;
        }
        if (cursor.getCount() != 0) {
            return toChange;
        }

        toChange.mName = _newName;

        int updatedRows = mResolver.update(Uri.parse(UnitProvider.SINGLE_UNIT_CONTENT_URI.replace("*", toChange.mUUID)), toChange.toContentValues(), null, null);

        if (updatedRows == 0) {
            return null;
        }

        mBus.post(new UnitChangedMessage(Change.CHANGED, toChange));
        cursor.close();
        return toChange;
    }

    @Override
    public boolean deleteUnit(Unit _unit, int _mode) {
        if (_unit == null) {
            return true;
        }
        Cursor cursor;
        IProductController productController = ControllerFactory.getProductController(mContext);

        switch (_mode) {
            case MODE_DELETE_REFERENCES:

                cursor = mResolver.query(Uri.parse(ProductProvider.MULTIPLE_PRODUCT_CONTENT_URI),
                        Product.PREFIXED_COLUMN.ALL_COLUMNS,
                        Product.PREFIXED_COLUMN.UNIT + "=?",
                        new String[]{_unit.mUUID}, null);

                if (cursor == null) {
                    Log.e(LOG_TAG, "MODE DELETE REFERENCES no cursor fetched.");
                    return false;
                }
                if(cursor.getCount() == 0){
                    cursor.close();
                    return true;
                }

                cursor.moveToFirst();
                do {
                    productController.removeProduct(productController.parseProduct(cursor), true);
                } while (cursor.moveToNext());

                break;
            case MODE_UNLINK_REFERENCES:
                cursor = mResolver.query(Uri.parse(ProductProvider.MULTIPLE_PRODUCT_CONTENT_URI),
                        Product.PREFIXED_COLUMN.ALL_COLUMNS,
                        Product.PREFIXED_COLUMN.UNIT + "=?",
                        new String[]{_unit.mUUID}, null);

                if (cursor == null) {
                    Log.e(LOG_TAG, "MODE_UNLINK_REFERENCES no cursor fetched.");
                    return false;
                }
                if(cursor.getCount() == 0){
                    cursor.close();
                    return true;
                }

                Unit defaultUnit = getDefaultUnit();
                cursor.moveToFirst();
                do {
                    Product product = productController.parseProduct(cursor);
                    product.mUnit = defaultUnit;
                    productController.modifyProduct(product);
                } while (cursor.moveToNext());

                break;
            case MODE_BREAK_DELETION:
                cursor = mResolver.query(Uri.parse(ProductProvider.MULTIPLE_PRODUCT_CONTENT_URI),
                        Product.PREFIXED_COLUMN.ALL_COLUMNS,
                        Product.PREFIXED_COLUMN.UNIT + "=?",
                        new String[]{_unit.mUUID}, null);

                if (cursor == null) {
                    Log.e(LOG_TAG, "MODE_BREAK_DELETION no cursor fetched.");
                    return false;
                }

                if (cursor.getCount() != 0) {
                    cursor.close();
                    return false;
                }

                break;
            default:
                return false;
        }

        cursor.close();

        int deletedRows = mResolver.delete(Uri.parse(UnitProvider.SINGLE_UNIT_CONTENT_URI.replace("*", _unit.mUUID)), null, null);
        if (deletedRows == 0) {
            return false;
        }

        mBus.post(new UnitChangedMessage(Change.DELETED, _unit));

        return true;
    }

    @Override
    public List<Unit> listAll(String _orderByColumn, boolean _asc) {
        List<Unit> unitArray = new ArrayList<>();
        Cursor cursor = mResolver.query(Uri.parse(UnitProvider.MULTIPLE_UNIT_CONTENT_URI),
                Unit.COLUMN.ALL_COLUMNS,
                null,
                null,
                _orderByColumn + " " + (_asc ? "ASC" : "DESC"));
        if (cursor == null || cursor.getCount() == 0) {
            if (cursor != null) {
                cursor.close();
            }
            return unitArray;
        }
        cursor.moveToFirst();

        do {
            unitArray.add(parse(cursor));
        } while (cursor.moveToNext());
        cursor.close();
        return unitArray;
    }

    public Unit parse(Cursor _cursor) {
        Unit unit = new Unit();
        unit.mUUID = _cursor.getString(_cursor.getColumnIndex(Unit.COLUMN.ID));
        unit.mName = _cursor.getString(_cursor.getColumnIndex(Unit.COLUMN.NAME));
        return unit;
    }

    @Override
    public Unit getDefaultUnit() {
        Cursor cursor = mResolver.query(Uri.parse(UnitProvider.SINGLE_UNIT_CONTENT_URI.replace("*", "-")),
                Unit.COLUMN.ALL_COLUMNS,
                null,
                null,
                null);

        if(cursor == null){
            return null;
        }
        Unit unit;

        if(cursor.getCount() == 0){
            ContentValues cv = new ContentValues();
            cv.put(Unit.COLUMN.ID, "-");
            cv.put(Unit.COLUMN.NAME, "-");

            Uri insertedDefaultUnit = mResolver.insert(Uri.parse(UnitProvider.SINGLE_UNIT_CONTENT_URI.replace("*", "-")),
                    cv
                    );
            if(insertedDefaultUnit==null){
                cursor.close();
                return null;
            }
            unit = new Unit();
            unit.mUUID = "-";
            unit.mName = "-";
        }else{
            cursor.moveToFirst();
            unit = parse(cursor);
        }
        cursor.close();
        return unit;
    }
}
