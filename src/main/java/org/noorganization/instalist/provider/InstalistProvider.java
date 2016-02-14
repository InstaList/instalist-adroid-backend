package org.noorganization.instalist.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.noorganization.instalist.provider.internal.CategoryProvider;
import org.noorganization.instalist.provider.internal.IInternalProvider;
import org.noorganization.instalist.provider.internal.IngredientProvider;
import org.noorganization.instalist.provider.internal.ListEntryProvider;
import org.noorganization.instalist.provider.internal.ProductProvider;
import org.noorganization.instalist.provider.internal.RecipeProvider;
import org.noorganization.instalist.provider.internal.ShoppingListProvider;
import org.noorganization.instalist.provider.internal.TagProvider;
import org.noorganization.instalist.provider.internal.TaggedProductProvider;
import org.noorganization.instalist.provider.internal.UnitProvider;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Created by damihe on 19.10.15.
 */
public class InstalistProvider extends ContentProvider {

    public final static String AUTHORITY = "org.noorganization.instalist.provider";

    public final static String BASE_VENDOR = "vnd.org.noorganization.instalist.";
    /**
     * The base content uri. Build a uri with the table paths.
     **/
    public final static Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private SQLiteDatabase mDatabase;
    private HashMap<String, IInternalProvider> mInternalProviders;

    @Override
    public boolean onCreate() {
        Context currentContext = getContext();
        if (currentContext != null) {
            String dbPath = currentContext.getFilesDir().getAbsolutePath() + File.separator +
                    "org.noorganization.instalist.provider.db";
            mDatabase = new DBOpenHelper(getContext(), dbPath).getWritableDatabase();
        } else {
            mDatabase = new DBOpenHelper(null, ":memory:").getWritableDatabase();
        }

        IInternalProvider categoryProvider = new CategoryProvider();
        IInternalProvider productProvider = new ProductProvider(getContext());
        IInternalProvider unitProvider = new UnitProvider(getContext());
        IInternalProvider tagProvider = new TagProvider(getContext());
        IInternalProvider taggedProductProvider = new TaggedProductProvider(getContext());
        IInternalProvider shoppingListProvider = new ShoppingListProvider(getContext());
        IInternalProvider listEntryProvider = new ListEntryProvider(getContext());
        IInternalProvider ingredientProvider = new IngredientProvider(getContext());
        IInternalProvider recipeProvider = new RecipeProvider(getContext());

        categoryProvider.onCreate(mDatabase);
        productProvider.onCreate(mDatabase);
        unitProvider.onCreate(mDatabase);
        tagProvider.onCreate(mDatabase);
        taggedProductProvider.onCreate(mDatabase);
        shoppingListProvider.onCreate(mDatabase);
        listEntryProvider.onCreate(mDatabase);
        ingredientProvider.onCreate(mDatabase);
        recipeProvider.onCreate(mDatabase);

        mInternalProviders = new HashMap<>();
        mInternalProviders.put("category", categoryProvider);
        mInternalProviders.put("product", productProvider);
        mInternalProviders.put("unit", unitProvider);
        mInternalProviders.put("tag", tagProvider);
        mInternalProviders.put("taggedProduct", taggedProductProvider);
        mInternalProviders.put("list", shoppingListProvider);
        mInternalProviders.put("entry", listEntryProvider);
        mInternalProviders.put("ingredient", ingredientProvider);
        mInternalProviders.put("recipe", recipeProvider);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri _uri, String[] _projection, String _selection,
                        String[] _selectionArgs, String _sortOrder) {
        IInternalProvider provider = getInternalProvider(_uri);
        if (provider != null) {
            return provider.query(_uri, _projection, _selection, _selectionArgs, _sortOrder);
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri _uri) {
        IInternalProvider provider = getInternalProvider(_uri);
        if (provider != null) {
            return provider.getType(_uri);
        }
        return null;
    }

    /**
     * Returns the right provider for the given uri. The decision is made by the first path segment.
     *
     * @param _uri The uri to provide content for.
     * @return Either the internal provider or null if uri is not okay or it's the wrong authority.
     */
    private IInternalProvider getInternalProvider(@NonNull Uri _uri) {
        if (!AUTHORITY.equals(_uri.getAuthority())) {
            return null;
        }

        List<String> pathSegments = _uri.getPathSegments();
        if (pathSegments.size() == 0) {
            return null;
        }

        String firstPart = pathSegments.get(0);
        if (mInternalProviders.containsKey(firstPart)) {
            return mInternalProviders.get(firstPart);
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri _uri, ContentValues _values) {
        IInternalProvider provider = getInternalProvider(_uri);
        if (provider != null) {
            Uri rtnUri = provider.insert(_uri, _values);
            if (rtnUri != null) {
                Context context = getContext();
                if (context != null) {
                    context.getContentResolver().notifyChange(rtnUri, null);
                }
            }
            return rtnUri;
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri _uri, String _selection, String[] _selectionArgs) {
        IInternalProvider provider = getInternalProvider(_uri);
        if (provider != null) {
            return provider.delete(_uri, _selection, _selectionArgs);
        }
        return 0;
    }

    @Override
    public int update(@NonNull Uri _uri, ContentValues _values, String _selection,
                      String[] _selectionArgs) {
        IInternalProvider provider = getInternalProvider(_uri);
        if (provider != null) {
            return provider.update(_uri, _values, _selection, _selectionArgs);
        }
        return 0;
    }
}
