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

package org.noorganization.instalist.utils;

import android.content.Context;
import android.util.Log;

import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.model.ListEntry;
import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.model.Unit;
import org.noorganization.instalist.presenter.IListController;
import org.noorganization.instalist.presenter.IUnitController;
import org.noorganization.instalist.presenter.implementation.ControllerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generator to create sample data for database. Use it in debug mode to get some test data.
 * Created by tinos_000 on 21.04.2015.
 */
public class DatabaseSeeder {

    private final static String LOG_TAG = DatabaseSeeder.class.getName();
    private final String SAMPLE_TAG = "SAMPLE";
    private final int PRODUCT_TEST_DATA_SIZE = 25;
    private final long PRODUCT_LIST_SEED = 324982340237840L;
    private Context mContext;
    private IListController mListController;

    // -----------------------------------------------------------------------
    // PRIVATE AREA
    // -----------------------------------------------------------------------

    public DatabaseSeeder(Context _context) {
        mContext = _context;
        mListController = ControllerFactory.getListController(mContext);
    }

    /**
     * Fill the database with some sample data.
     */
    public void startUp() {
        // just for safety to delete whole product set
        tearDown();
        Random rand = new Random(PRODUCT_LIST_SEED);
        String[] categoryNames = new String[]{SAMPLE_TAG.concat("_ShoppingMall"), SAMPLE_TAG.concat("_ToolMarket")};
        String[] listNames = new String[]{SAMPLE_TAG.concat("_Home"), SAMPLE_TAG.concat("_Work")};
        String[] listProductNames = new String[]{"Sugar", "Beer", "Cheese", "Ham", "Nails", "Grenade Apple Juice"};
        String[] listUnitNames = new String[]{"g", "kg", "ml", "l", "hl", "pfund"};
        List<Product> productList = new ArrayList<>();
        List<ShoppingList> shoppingLists = new ArrayList<>();
        List<Category> categoryList = new ArrayList<>();

        for (String categoryName : categoryNames) {
            categoryList.add(ControllerFactory.getCategoryController(mContext).createCategory(categoryName));
        }

        // add new lists
        for (String listName : listNames) {
            shoppingLists.add(mListController.addList(listName, categoryList.get(0)));
            Log.d(LOG_TAG, "List name: " + listName);
            //shoppingLists[counter].save();
        }

        IUnitController unitController = ControllerFactory.getUnitController(mContext);
        for (String unitName : listUnitNames) {
            Unit createdUnit = unitController.createUnit(unitName);
            Log.d(LOG_TAG, "Added unit: " + (createdUnit == null ? "FAILED! " : "") + unitName);
        }


        for (int Index = 0; Index < PRODUCT_TEST_DATA_SIZE; ++Index) {
            String productName;
            Product singleProduct;
            Unit singleUnit;

            productName = listProductNames[rand.nextInt(listProductNames.length)].concat("_" + String.valueOf(Index));
            // singleUnit      = new Unit(listUnitNames[rand.nextInt(listUnitNames.length)]);
            // singleUnit.save();

            singleProduct = ControllerFactory.getProductController(mContext).createProduct(productName, null, 1.0f, 1.0f);// new Product(productName, null, 1.0f, 1.0f);

            //singleProduct.save();
            productList.add(singleProduct);
        }

        for (Product product : productList) {
            Log.d(LOG_TAG, "Add Product: " + product.mName);

            //mListController.addOrChangeItem(shoppingLists[rand.nextInt(shoppingLists.length)], product, rand.nextFloat());
            ShoppingList list = shoppingLists.get(rand.nextInt(shoppingLists.size()));
            Log.d(LOG_TAG, "List name: " + list.mName);
            ListEntry listEntry = ControllerFactory.getListController(mContext).addOrChangeItem(list, product, rand.nextFloat() * 100.0f);
            listEntry.mStruck = false;
            listEntry = null;
        }
    }

    /**
     * Delete all sample data from database.
     */
    public void tearDown() {
        /*List<ShoppingList>  shoppingLists  = ShoppingList.listAll(ShoppingList.class);
        List<ListEntry>     listEntries    = ListEntry.listAll(ListEntry.class);
        List<Product>       products        = Product.listAll(Product.class);

        Category.deleteAll(Category.class);
        ListEntry.deleteAll(ListEntry.class);
        ShoppingList.deleteAll(ShoppingList.class);
        Product.deleteAll(Product.class);
        Ingredient.deleteAll(Ingredient.class);
        Recipe.deleteAll(Recipe.class);
        Tag.deleteAll(Tag.class);
        TaggedProduct.deleteAll(TaggedProduct.class);
        Unit.deleteAll(Unit.class);*/
    }
}
