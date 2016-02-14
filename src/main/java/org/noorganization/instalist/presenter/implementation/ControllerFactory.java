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
