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

package org.noorganization.instalist.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.noorganization.instalist.types.ModelType;
import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.model.Ingredient;
import org.noorganization.instalist.model.ListEntry;
import org.noorganization.instalist.model.LogInfo;
import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.Recipe;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.model.Tag;
import org.noorganization.instalist.model.TaggedProduct;
import org.noorganization.instalist.model.Unit;

/**
 * Helps to open database in the provider.
 * Created by damihe on 21.10.15.
 */
public class DBOpenHelper extends SQLiteOpenHelper {

    public static int VERSION = 3;

    public DBOpenHelper(Context _context, String _name) {
        super(_context, _name, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase _db) {
        _db.execSQL(Category.DB_CREATE);
        _db.execSQL(ShoppingList.DB_CREATE);
        _db.execSQL(Unit.DATABASE_CREATE);
        _db.execSQL(Product.DATABASE_CREATE);
        _db.execSQL(ListEntry.DB_CREATE);
        _db.execSQL(Tag.DATABASE_CREATE);
        _db.execSQL(TaggedProduct.DATABASE_CREATE);
        _db.execSQL(Ingredient.DATABASE_CREATE);
        _db.execSQL(Recipe.DATABASE_CREATE);
    }

    @Override
    public void onOpen(SQLiteDatabase _db) {
        super.onOpen(_db);
        if (!_db.isReadOnly()) {
            _db.execSQL("PRAGMA foreign_keys = ON");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
        int currentVersion = _oldVersion;
        // Example:
        if (currentVersion < 3 && 3 <= _newVersion) {
            // do upgrade to version 3
            // inroduction of triggers and logging db
            _db.execSQL(LogInfo.DB_CREATE_V1);

            // create trigger category
            _db.execSQL(LogInfo.createTriggerInsertion(Category.TABLE_NAME, Category.COLUMN.ID, ModelType.CATEGORY));
            _db.execSQL(LogInfo.createTriggerDeletion(Category.TABLE_NAME, Category.COLUMN.ID, ModelType.CATEGORY));
            _db.execSQL(LogInfo.createTriggerUpdate(Category.TABLE_NAME, Category.COLUMN.ID, ModelType.CATEGORY));

            // create trigger list
            _db.execSQL(LogInfo.createTriggerInsertion(ShoppingList.TABLE_NAME, ShoppingList.COLUMN.ID, ModelType.LIST));
            _db.execSQL(LogInfo.createTriggerDeletion(ShoppingList.TABLE_NAME, ShoppingList.COLUMN.ID, ModelType.LIST));
            _db.execSQL(LogInfo.createTriggerUpdate(ShoppingList.TABLE_NAME, ShoppingList.COLUMN.ID, ModelType.LIST));

            // create trigger product
            _db.execSQL(LogInfo.createTriggerInsertion(Product.TABLE_NAME, Product.COLUMN.ID, ModelType.PRODUCT));
            _db.execSQL(LogInfo.createTriggerDeletion(Product.TABLE_NAME, Product.COLUMN.ID, ModelType.PRODUCT));
            _db.execSQL(LogInfo.createTriggerUpdate(Product.TABLE_NAME, Product.COLUMN.ID, ModelType.PRODUCT));

            // create trigger taggedproduct
            _db.execSQL(LogInfo.createTriggerInsertion(TaggedProduct.TABLE_NAME, TaggedProduct.COLUMN.ID, ModelType.TAGGED_PRODUCT));
            _db.execSQL(LogInfo.createTriggerDeletion(TaggedProduct.TABLE_NAME, TaggedProduct.COLUMN.ID, ModelType.TAGGED_PRODUCT));
            _db.execSQL(LogInfo.createTriggerUpdate(TaggedProduct.TABLE_NAME, TaggedProduct.COLUMN.ID, ModelType.TAGGED_PRODUCT));

            // create trigger list entry
            _db.execSQL(LogInfo.createTriggerInsertion(ListEntry.TABLE_NAME, ListEntry.COLUMN.ID, ModelType.LIST_ENTRY));
            _db.execSQL(LogInfo.createTriggerDeletion(ListEntry.TABLE_NAME, ListEntry.COLUMN.ID, ModelType.LIST_ENTRY));
            _db.execSQL(LogInfo.createTriggerUpdate(ListEntry.TABLE_NAME, ListEntry.COLUMN.ID, ModelType.LIST_ENTRY));

            // create trigger ingredient
            _db.execSQL(LogInfo.createTriggerInsertion(Ingredient.TABLE_NAME, Ingredient.COLUMN.ID, ModelType.INGREDIENT));
            _db.execSQL(LogInfo.createTriggerDeletion(Ingredient.TABLE_NAME, Ingredient.COLUMN.ID, ModelType.INGREDIENT));
            _db.execSQL(LogInfo.createTriggerUpdate(Ingredient.TABLE_NAME, Ingredient.COLUMN.ID, ModelType.INGREDIENT));

            // create trigger recipe
            _db.execSQL(LogInfo.createTriggerInsertion(Recipe.TABLE_NAME, Recipe.COLUMN.ID, ModelType.RECIPE));
            _db.execSQL(LogInfo.createTriggerDeletion(Recipe.TABLE_NAME, Recipe.COLUMN.ID, ModelType.RECIPE));
            _db.execSQL(LogInfo.createTriggerUpdate(Recipe.TABLE_NAME, Recipe.COLUMN.ID, ModelType.RECIPE));

            // create trigger unit
            _db.execSQL(LogInfo.createTriggerInsertion(Unit.TABLE_NAME, Unit.COLUMN.ID, ModelType.UNIT));
            _db.execSQL(LogInfo.createTriggerDeletion(Unit.TABLE_NAME, Unit.COLUMN.ID, ModelType.UNIT));
            _db.execSQL(LogInfo.createTriggerUpdate(Unit.TABLE_NAME, Unit.COLUMN.ID, ModelType.UNIT));

            // create trigger unit
            _db.execSQL(LogInfo.createTriggerInsertion(Tag.TABLE_NAME, Tag.COLUMN.ID, ModelType.TAG));
            _db.execSQL(LogInfo.createTriggerDeletion(Tag.TABLE_NAME, Tag.COLUMN.ID, ModelType.TAG));
            _db.execSQL(LogInfo.createTriggerUpdate(Tag.TABLE_NAME, Tag.COLUMN.ID, ModelType.TAG));
        }
    }
}
