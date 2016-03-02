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

package org.noorganization.instalist.presenter.implementation;

import android.content.Context;

import org.noorganization.instalist.presenter.ICategoryController;
import org.noorganization.instalist.presenter.IListController;
import org.noorganization.instalist.presenter.IProductController;
import org.noorganization.instalist.presenter.IRecipeController;
import org.noorganization.instalist.presenter.ITagController;
import org.noorganization.instalist.presenter.IUnitController;

/**
 * Holds instances of IXController (insert something for X), which themself are singletons. This
 * class makes it harder to use the wrong presenter class and the module itself gets more portable,
 * since the getInstance-Methods don't have to be public and the tested interfaces have to be used.
 * Created by Michi on 11.05.2015.
 */
public class ControllerFactory {
    public static IListController getListController(Context _context) {
        return ListController.getInstance(_context);
    }

    public static IProductController getProductController(Context _context) {
        return ProductController.getInstance(_context);
    }

    public static IRecipeController getRecipeController(Context _context) {
        return RecipeController.getInstance(_context);
    }

    public static IUnitController getUnitController(Context _context) {
        return UnitController.getInstance(_context);
    }

    public static ITagController getTagController(Context _context) {
        return TagController.getInstance(_context);
    }

    public static ICategoryController getCategoryController(Context _context) {
        return CategoryController.getInstance(_context);
    }

}
