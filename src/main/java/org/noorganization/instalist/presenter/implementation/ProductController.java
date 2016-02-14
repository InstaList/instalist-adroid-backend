package org.noorganization.instalist.presenter.implementation;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import org.noorganization.instalist.presenter.IProductController;
import org.noorganization.instalist.presenter.ITagController;
import org.noorganization.instalist.presenter.IUnitController;
import org.noorganization.instalist.presenter.event.Change;
import org.noorganization.instalist.presenter.event.ProductChangedMessage;
import org.noorganization.instalist.model.Ingredient;
import org.noorganization.instalist.model.ListEntry;
import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.Tag;
import org.noorganization.instalist.model.TaggedProduct;
import org.noorganization.instalist.model.Unit;
import org.noorganization.instalist.provider.InstalistProvider;
import org.noorganization.instalist.provider.internal.IngredientProvider;
import org.noorganization.instalist.provider.internal.ProductProvider;
import org.noorganization.instalist.provider.internal.TaggedProductProvider;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


public class ProductController implements IProductController {

    private static String LOG_TAG = ProductController.class.getName();
    private static ProductController mInstance;

    private EventBus mBus;
    private Context mContext;
    private ContentResolver mResolver;
    private ITagController mTagController;

    private ProductController(Context _context) {
        mBus = EventBus.getDefault();
        mContext = _context;
        mResolver = mContext.getContentResolver();
        mTagController = ControllerFactory.getTagController(_context);
    }

    static ProductController getInstance(Context _context) {
        if (mInstance == null) {
            mInstance = new ProductController(_context);
        }
        return mInstance;
    }

    @Override
    public Product createProduct(String _name, Unit _unit, float _defaultAmount, float _stepAmount) {
        if (_name == null || _name.length() == 0) {
            return null;
        }
        if (Float.isInfinite(_defaultAmount) || Float.isNaN(_defaultAmount) || _defaultAmount <= 0.0f) {
            return null;
        }
        if (Float.isInfinite(_stepAmount) || Float.isNaN(_stepAmount) || _stepAmount < 0.0f) {
            return null;
        }
        Unit unit;

        if (_unit == null) {
            unit = ControllerFactory.getUnitController(mContext).getDefaultUnit();
        } else {
            unit = ControllerFactory.getUnitController(mContext).findById(_unit.mUUID);
        }

        if (unit == null) {
            return null;
        }

        Cursor productCursor = mResolver.query(Uri.parse(ProductProvider.MULTIPLE_PRODUCT_CONTENT_URI), Product.COLUMN.ALL_COLUMNS, Product.COLUMN.NAME + "= ?", new String[]{_name}, null, null);
        if (productCursor == null || productCursor.getCount() > 0) {
            return null;
        }

        Product product = new Product();
        product.mName = _name;
        product.mDefaultAmount = _defaultAmount;
        product.mStepAmount = _stepAmount;
        product.mUnit = unit;

        Uri productUri = mResolver.insert(Uri.parse(ProductProvider.MULTIPLE_PRODUCT_CONTENT_URI), product.toContentValues());
        if (productUri == null) {
            return null;
        }

        product.mUUID = productUri.getLastPathSegment();
        mBus.post(new ProductChangedMessage(Change.CREATED, product));

        productCursor.close();
        return product;
    }

    @Override
    public Product findById(@NonNull String _uuid) {
        Cursor productCursor = mResolver.query(Uri.parse(ProductProvider.SINGLE_PRODUCT_CONTENT_URI.replace("*", _uuid)),
                Product.COLUMN.ALL_COLUMNS, null, null, null);
        if (productCursor == null || productCursor.getCount() != 1) {
            return null;
        }
        productCursor.moveToFirst();
        Product product = new Product();
        product.mName = productCursor.getString(productCursor.getColumnIndex(Product.COLUMN.NAME));
        product.mUUID = productCursor.getString(productCursor.getColumnIndex(Product.COLUMN.ID));
        product.mDefaultAmount = productCursor.getFloat(productCursor.getColumnIndex(Product.COLUMN.DEFAULT_AMOUNT));
        product.mStepAmount = productCursor.getFloat(productCursor.getColumnIndex(Product.COLUMN.STEP_AMOUNT));

        IUnitController unitController = ControllerFactory.getUnitController(mContext);
        String unitID = productCursor.getString(productCursor.getColumnIndex(Product.COLUMN.UNIT));
        if(unitID == null){
            product.mUnit = null;
        } else {
            product.mUnit = ControllerFactory.getUnitController(mContext).findById(unitID);
        }

        /*
        if(product.mUnit == null){
            return null;
        }
        */
        productCursor.close();
        return product;
    }

    @Override
    public Product modifyProduct(Product _toChange) {
        if (_toChange == null) {
            return null;
        }
        Product oldProduct = this.findById(_toChange.mUUID);
        if (oldProduct == null) {
            return null;
        }
        if (_toChange.mName == null || _toChange.mName.length() == 0) {
            return oldProduct;
        }
        if (Float.isInfinite(_toChange.mDefaultAmount) || Float.isNaN(_toChange.mDefaultAmount) ||
                _toChange.mDefaultAmount <= 0.0f) {
            return oldProduct;
        }
        if (Float.isInfinite(_toChange.mStepAmount) || Float.isNaN(_toChange.mStepAmount) ||
                _toChange.mStepAmount < 0.0f) {
            return oldProduct;
        }
        // check if given unit exists
        if (_toChange.mUnit != null && ControllerFactory.getUnitController(mContext).findById(_toChange.mUnit.mUUID) == null) {
            return oldProduct;
        }

        // check if another product has the same name if this name was changed
        Cursor productsWithSameName = mResolver.query(Uri.parse(ProductProvider.MULTIPLE_PRODUCT_CONTENT_URI),
                Product.COLUMN.ALL_COLUMNS, Product.COLUMN.NAME + "= ? AND " + Product.COLUMN.ID + "<> ?", new String[]{_toChange.mName, _toChange.mUUID}, null, null);
        if (productsWithSameName == null) {
            Log.e(LOG_TAG, "Internal failure ");
            return oldProduct;
        }
        // check if there is another item with the same name
        if (productsWithSameName.getCount() > 0) {
            return oldProduct;
        }
        productsWithSameName.close();

        _toChange.mUUID = oldProduct.mUUID;

        int updatedRows = mResolver.update(Uri.parse(ProductProvider.SINGLE_PRODUCT_CONTENT_URI.replace("*", _toChange.mUUID)), _toChange.toContentValues(),null,null);
        if (updatedRows == 0) {
            return oldProduct;
        }

        mBus.post(new ProductChangedMessage(Change.CHANGED, _toChange));

        return _toChange;
    }

    @Override
    public boolean removeProduct(Product _toRemove, boolean _deleteCompletely) {
        if (_toRemove == null) {
            return false;
        }

        Product foundProduct = findById(_toRemove.mUUID);
        if (foundProduct == null) {
            // no product to delete
            return false;
        }

        Cursor listEntryCursor = mResolver.query(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "entry"), ListEntry.COLUMN.ALL_COLUMNS, ListEntry.COLUMN.PRODUCT + "= ?", new String[]{_toRemove.mUUID}, null, null);
        Cursor ingredientCursor = mResolver.query(Uri.parse(IngredientProvider.MULTIPLE_INGREDIENT_CONTENT_URI), Ingredient.COLUMN.ALL_COLUMNS, Ingredient.COLUMN.PRODUCT_ID + "= ?", new String[]{_toRemove.mUUID}, null, null);
        if (listEntryCursor == null) {
            Log.e(LOG_TAG, "Query for listEntry was not possible in removeProduct.");
            return false;
        }

        if (ingredientCursor == null) {
            Log.e(LOG_TAG, "Query for ingredient was not possible in removeProduct.");
            return false;
        }

        if (!_deleteCompletely) {
            if (listEntryCursor.getCount() > 0) {
                Log.e(LOG_TAG, "There are still ListEntries connected to this product. Either set deleteCompletely flag or remove the ListEntries by yourself.");
                listEntryCursor.close();
                return false;
            }

            if (ingredientCursor.getCount() > 0) {
                Log.e(LOG_TAG, "There are still ingredients connected to this product. Either set deleteCompletely flag or remove the ingredients by your self.");
                ingredientCursor.close();
                return false;
            }
        }

        // TODO this seems really obsolete and will be a bottleneck if we want to delet each single entry
/*        listEntryCursor.moveToFirst();
        ingredientCursor.moveToFirst();
        do {
            ListEntry entry = new ListEntry();
            entry.mUUID = listEntryCursor.getString(listEntryCursor.getColumnIndex(ListEntry.COLUMN.ID));
            entry.mAmount = listEntryCursor.getFloat(listEntryCursor.getColumnIndex(ListEntry.COLUMN.AMOUNT));
            entry.mPriority = listEntryCursor.getInt(listEntryCursor.getColumnIndex(ListEntry.COLUMN.PRIORITY));
            entry.mStruck = listEntryCursor.getInt(listEntryCursor.getColumnIndex(ListEntry.COLUMN.STRUCK)) > 0;
            entry.mList =
            listEntryCursor.getString();
            mBus.post(new ListItemChangedMessage(Change.DELETED, currentEntry));
        } while(listEntryCursor.moveToNext());

        for (ListEntry currentEntry : foundEntries) {
            mBus.post(new ListItemChangedMessage(Change.DELETED, currentEntry));
        }
        for (Ingredient currentIngredient : foundIngredients) {
            mBus.post(new RecipeChangedMessage(Change.CHANGED,
                    currentIngredient.mRecipe));
        }
*/

        int affectedProducts = mResolver.delete(Uri.parse(ProductProvider.SINGLE_PRODUCT_CONTENT_URI.replace("*", _toRemove.mUUID)), null, null);

        if (affectedProducts > 0) {
            mBus.post(new ProductChangedMessage(Change.DELETED, foundProduct));
        }

        listEntryCursor.close();
        ingredientCursor.close();
        return true;
    }

    @Override
    public TaggedProduct addTagToProduct(Product _product, Tag _tag) {
        if (_product == null || _tag == null) {
            return null;
        }
        Product foundProduct = findById(_product.mUUID);
        Tag foundTag = mTagController.findById(_tag.mUUID);
        if (foundProduct == null || foundTag == null) {
            return null;
        }

        Cursor taggedProductCursor = mResolver.query(Uri.parse(TaggedProductProvider.MULTIPLE_TAGGED_PRODUCT_BY_TAG_CONTENT_URI.replace("*", foundTag.mUUID)),
                TaggedProduct.ALL_COLUMNS_JOINED,
                TaggedProduct.COLUMN_PREFIXED.PRODUCT_ID + "=? AND " + TaggedProduct.COLUMN_PREFIXED.TAG_ID + "=?",
                new String[]{foundProduct.mUUID, foundTag.mUUID}, null);

        if (taggedProductCursor == null) {
            return null;
        }

        // check if this {@link TaggedProduct} exists already in the database.
        if (taggedProductCursor.getCount() == 0) {
            TaggedProduct newTaggedProduct = new TaggedProduct(_tag, _product);
            Uri taggedProductUri = mResolver.insert(Uri.parse(TaggedProductProvider.MULTIPLE_TAGGED_PRODUCT_CONTENT_URI), newTaggedProduct.toContentValues());
            if (taggedProductUri == null) {
                taggedProductCursor.close();
                return null;
            }
            newTaggedProduct.mUUID = taggedProductUri.getLastPathSegment();
            mBus.post(new ProductChangedMessage(Change.CHANGED, foundProduct));
            taggedProductCursor.close();
            return newTaggedProduct;
        }
        TaggedProduct taggedProduct = new TaggedProduct(_tag, _product);
        taggedProduct.mUUID = taggedProductCursor.getString(taggedProductCursor.getColumnIndex(TaggedProduct.COLUMN_PREFIXED.ID));

        taggedProductCursor.close();
        return taggedProduct;
    }

    @Override
    public void removeTagFromProduct(Product _product, Tag _tag) {
        if (_product == null || _tag == null) {
            return;
        }

        Product foundProduct = findById(_product.mUUID);
        if (foundProduct == null) {
            return;
        }

        Tag foundTag = mTagController.findById(_tag.mUUID);
        if (foundTag == null) {
            return;
        }

        Cursor taggedProductCursor = mResolver.query(Uri.parse(TaggedProductProvider.MULTIPLE_TAGGED_PRODUCT_BY_TAG_CONTENT_URI.replace("*", foundTag.mUUID)),
                TaggedProduct.ALL_COLUMNS_JOINED,
                TaggedProduct.COLUMN_PREFIXED.PRODUCT_ID + "=?",
                new String[]{foundProduct.mUUID}, null);

        if (taggedProductCursor == null) {
            return;
        }

        if (taggedProductCursor.getCount() != 0) {
            taggedProductCursor.moveToFirst();
            do {
                TaggedProduct taggedProduct = parse(taggedProductCursor);
                mResolver.delete(Uri.parse(TaggedProductProvider.SINGLE_TAGGED_PRODUCT_CONTENT_URI.replace("*", taggedProduct.mUUID)), null, null);
            } while (taggedProductCursor.moveToNext());

            mBus.post(new ProductChangedMessage(Change.CHANGED, foundProduct));
        }
    }


    public Product parseProduct(Cursor _cursor) {
        Product product = new Product();
        product.mUUID = _cursor.getString(_cursor.getColumnIndex(Product.COLUMN.ID));
        product.mName = _cursor.getString(_cursor.getColumnIndex(Product.COLUMN.NAME));
        product.mUUID = _cursor.getString(_cursor.getColumnIndex(Product.COLUMN.ID));
        product.mDefaultAmount = _cursor.getFloat(_cursor.getColumnIndex(Product.COLUMN.DEFAULT_AMOUNT));
        product.mStepAmount = _cursor.getFloat(_cursor.getColumnIndex(Product.COLUMN.STEP_AMOUNT));
        return product;
    }

    public TaggedProduct parse(Cursor _cursor) {
        TaggedProduct taggedProduct = new TaggedProduct();

        Tag tag = new Tag();
        tag.mUUID = _cursor.getString(_cursor.getColumnIndex(TaggedProduct.COLUMN.TAG_ID));
        tag.mName = _cursor.getString(_cursor.getColumnIndex(Tag.COLUMN.NAME));

        Product product = parseProduct(_cursor);

        taggedProduct.mTag = tag;
        taggedProduct.mProduct = product;

        return taggedProduct;
    }

    /**
     * Retrieves all tagged products for the given _product.
     *
     * @param _product the product where the associating TaggedProduct should be found.
     * @return the TaggedProduct when found, else empty TaggedProductList.
     * @deprecated it seems to be a weird method
     */
    @Override
    public List<TaggedProduct> findTaggedProductsByProduct(Product _product) {
        Cursor taggedProductCursor = mResolver.query(Uri.parse(TaggedProductProvider.MULTIPLE_TAGGED_PRODUCT_CONTENT_URI),
                TaggedProduct.ALL_COLUMNS_JOINED,
                TaggedProduct.COLUMN_PREFIXED.PRODUCT_ID + "=?",
                new String[]{_product.mUUID}, null);

        if (taggedProductCursor == null) {
            return null;
        }
        List<TaggedProduct> taggedProducts = new ArrayList<>(taggedProductCursor.getCount());
        if(taggedProductCursor.getCount() == 0){
            taggedProductCursor.close();
            return taggedProducts;
        }
        taggedProductCursor.moveToFirst();
        do {
            taggedProducts.add(parse(taggedProductCursor));
        } while (taggedProductCursor.moveToNext());
        taggedProductCursor.close();
        return taggedProducts;
    }

    @Override
    public List<Product> listAll() {
        Cursor productCursor = mResolver.query(Uri.parse(ProductProvider.MULTIPLE_PRODUCT_CONTENT_URI),
                Product.COLUMN.ALL_COLUMNS,
                null,
                null,
                null);

        if (productCursor == null) {
            return null;
        }

        productCursor.moveToFirst();
        List<Product> products = new ArrayList<>(productCursor.getCount());
        if(productCursor.getCount() == 0){
            productCursor.close();
            return products;
        }

        do {
            products.add(parseProduct(productCursor));
        } while (productCursor.moveToNext());
        productCursor.close();
        return products;
    }

}
