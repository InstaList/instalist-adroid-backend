package org.noorganization.instalist.presenter.implementation;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import org.noorganization.instalist.presenter.IProductController;
import org.noorganization.instalist.presenter.IRecipeController;
import org.noorganization.instalist.presenter.event.Change;
import org.noorganization.instalist.presenter.event.RecipeChangedMessage;
import org.noorganization.instalist.model.Ingredient;
import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.Recipe;
import org.noorganization.instalist.provider.internal.IngredientProvider;
import org.noorganization.instalist.provider.internal.RecipeProvider;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class RecipeController implements IRecipeController {

    private static String LOG_TAG = RecipeController.class.getName();
    private static RecipeController mInstance;

    private EventBus mBus;
    private ContentResolver mResolver;
    private IProductController mProductController;

    static IRecipeController getInstance(Context _context) {
        if (mInstance == null) {
            mInstance = new RecipeController(_context);
        }
        return mInstance;
    }

    private RecipeController(Context _context) {
        mBus = EventBus.getDefault();
        mResolver = _context.getContentResolver();
        mProductController = ControllerFactory.getProductController(_context);
    }

    @Override
    public Recipe createRecipe(String _name) {
        if (_name == null) {
            return null;
        }
        // check if recipe already exists
        if (findByName(_name) != null) {
            Log.e(LOG_TAG, "createRecipe: Creation of recipe failed because name is already taken.");
            return null;
        }

        Recipe rtn = new Recipe(_name);
        Uri recipeUri = mResolver.insert(Uri.parse(RecipeProvider.MULTIPLE_RECIPE_CONTENT_URI), rtn.toContentValues());

        if (recipeUri == null) {
            Log.e(LOG_TAG, "createRecipe: Insertion of recipe failed.");
            return null;
        }

        rtn.mUUID = recipeUri.getLastPathSegment();
        mBus.post(new RecipeChangedMessage(Change.CREATED, rtn));

        return rtn;
    }

    @Override
    public Recipe renameRecipe(Recipe _toChange, String _newName) {
        if (_newName == null || _toChange == null) {
            return null;
        }

        Recipe toRename = findById(_toChange.mUUID);
        if (toRename == null) {
            return _toChange;
        }

        Cursor recipeCursor = mResolver.query(Uri.parse(RecipeProvider.MULTIPLE_RECIPE_CONTENT_URI),
                Recipe.COLUMN.ALL_COLUMNS,
                Recipe.COLUMN.NAME + " = ? AND " + Recipe.COLUMN.ID + "<> ?",
                new String[]{_newName, toRename.mUUID},
                null,
                null
        );

        // check if there is no further recipe with this name
        if (recipeCursor == null || recipeCursor.getCount() != 0) {
            if (recipeCursor != null) {
                recipeCursor.close();
            }
            return _toChange;
        }
        recipeCursor.close();

        toRename.mName = _newName;

        int updatedRows = mResolver.update(Uri.parse(RecipeProvider.SINGLE_RECIPE_CONTENT_URI.replace("*", toRename.mUUID)),
                toRename.toContentValues(),
                null,
                null);

        if (updatedRows == 0) {
            Log.e(LOG_TAG, "Update Recipe name went wrong, no row was updated.");
            return _toChange;
        }

        mBus.post(new RecipeChangedMessage(Change.CHANGED, toRename));

        return toRename;
    }

    @Override
    public void removeRecipe(Recipe _toRemove) {
        if (_toRemove == null) {
            return;
        }

        Recipe toDelete = findById(_toRemove.mUUID);
        if (toDelete != null) {
            int affectedRows = mResolver.delete(Uri.parse(RecipeProvider.MULTIPLE_RECIPE_INGREDIENT_CONTENT_URI.replace("*", toDelete.mUUID)),
                    null,
                    null
            );
            Log.i(LOG_TAG, "Deleted " + affectedRows + " ingredients");
            affectedRows = mResolver.delete(Uri.parse(RecipeProvider.SINGLE_RECIPE_CONTENT_URI.replace("*", toDelete.mUUID)),
                    null,
                    null);

            if (affectedRows == 0) {
                Log.e(LOG_TAG, "No recipe deleted.");
                return;
            }
        } else {
            Log.d(LOG_TAG, "No Recipe found.");
            return;
        }

        mBus.post(new RecipeChangedMessage(Change.DELETED, toDelete));
    }

    @Override
    public Ingredient addOrChangeIngredient(Recipe _recipe, Product _productToAdd, float _amount) {
        if (_recipe == null || _productToAdd == null) {
            return null;
        }


        Recipe savedRecipe = findById(_recipe.mUUID);
        Product savedProduct = mProductController.findById(_productToAdd.mUUID);

        if (savedProduct == null || savedRecipe == null) {
            return null;
        }

        Cursor ingredientCursor = mResolver.query(Uri.parse(IngredientProvider.MULTIPLE_INGREDIENT_CONTENT_URI),
                Ingredient.COLUMN.ALL_COLUMNS,
                Ingredient.COLUMN.PRODUCT_ID + " =? AND " + Ingredient.COLUMN.RECIPE_ID + "=?",
                new String[]{savedProduct.mUUID, savedRecipe.mUUID},
                null,
                null
        );

        if (ingredientCursor == null) {
            Log.e(LOG_TAG, "AddOrChangeIngredient: No ingredientcursor can be fetched.");
            return null;
        }

        Ingredient ingredient;
        if (ingredientCursor.getCount() == 0) {
            if (_amount < 0.001f) {
                ingredientCursor.close();
                return null;
            }
            ingredient = new Ingredient(savedProduct, savedRecipe, _amount);
            Uri ingredientUri = mResolver.insert(Uri.parse(RecipeProvider.MULTIPLE_RECIPE_INGREDIENT_CONTENT_URI.replace("*", savedRecipe.mUUID)),
                    ingredient.toContentValues());
            if (ingredientUri == null) {
                Log.e(LOG_TAG, "AddOrChangeIngredient: insert ingredient went wrong.");
                ingredientCursor.close();
                return null;
            }
            ingredient.mUUID = ingredientUri.getLastPathSegment();
        } else {
            ingredientCursor.moveToFirst();
            ingredient = parse(ingredientCursor);

            if (_amount < 0.001f) {
                ingredientCursor.close();
                return ingredient;
            }

            ingredient.mAmount = _amount;
            int updatedElements = mResolver.update(Uri.parse(RecipeProvider.SINGLE_RECIPE_INGREDIENT_CONTENT_URI.replaceFirst("\\*", savedRecipe.mUUID).replace("*", ingredient.mUUID)),
                    ingredient.toContentValues(),
                    null,
                    null);
            if (updatedElements == 0) {
                Log.d(LOG_TAG, "AddOrChangeIngredient: update ingredient went wrong. Nothing gets updated.");
                ingredientCursor.close();
                return null;
            }
        }
        ingredientCursor.close();
        return ingredient;
    }

    @Override
    public void removeIngredient(Recipe _recipe, Product _productToRemove) {
        if (_recipe == null || _productToRemove == null) {
            return;
        }

        int deletedRows = mResolver.delete(Uri.parse(IngredientProvider.MULTIPLE_INGREDIENT_CONTENT_URI),
                Ingredient.COLUMN.PRODUCT_ID + " =? AND " + Ingredient.COLUMN.RECIPE_ID + "=?",
                new String[]{_productToRemove.mUUID, _recipe.mUUID}
        );

        if (deletedRows == 0) {
            Log.e(LOG_TAG, "removeIngredient: No ingredient deleted.");
        }
    }

    @Override
    public void removeIngredient(Ingredient _toRemove) {
        if (_toRemove == null) {
            return;
        }

        int removedIngredients = mResolver.delete(Uri.parse(IngredientProvider.SINGLE_INGREDIENT_CONTENT_URI.replace("*", _toRemove.mUUID)),
                null,
                null);

        if (removedIngredients == 0) {
            Log.e(LOG_TAG, "No ingredient deleted: " + _toRemove.mUUID);
        }
    }


    @Override
    public Recipe findById(String _uuid) {
        if (_uuid == null) {
            return null;
        }
        Cursor recipeCursor = mResolver.query(Uri.parse(RecipeProvider.SINGLE_RECIPE_CONTENT_URI.replace("*", _uuid)),
                Recipe.COLUMN.ALL_COLUMNS,
                null,
                null,
                null
        );

        if (recipeCursor == null) {
            Log.e(LOG_TAG, "Recipe findById: cannot fetch recipecursor or ingredientcursor");
            return null;
        }

        if (recipeCursor.getCount() == 0) {
            Log.e(LOG_TAG, "Recipe findById: There is no recipe with the id: " + _uuid + " in the database.");
            return null;
        }

        recipeCursor.moveToFirst();
        Recipe recipe = new Recipe();
        recipe.mUUID = _uuid;
        recipe.mName = recipeCursor.getString(recipeCursor.getColumnIndex(Recipe.COLUMN.NAME));

        recipeCursor.close();
        return recipe;
    }

    @Override
    public Recipe findByName(@NonNull String _name) {
        if (_name == null) {
            // NonNull-Annotation does not work in every case.
            return null;
        }

        Cursor recipeCursor = mResolver.query(Uri.parse(RecipeProvider.MULTIPLE_RECIPE_CONTENT_URI),
                Recipe.COLUMN.ALL_COLUMNS,
                Recipe.COLUMN.NAME + " = ?",
                new String[]{_name},
                null
        );

        if (recipeCursor == null) {
            Log.e(LOG_TAG, "Recipe findByName: cannot fetch recipecursor or ingredientcursor");
            return null;
        }

        if (recipeCursor.getCount() == 0) {
            Log.e(LOG_TAG, "Recipe findByName: There is no recipe with the name: " + _name + " in the database.");
            recipeCursor.close();
            return null;
        }

        recipeCursor.moveToFirst();
        Recipe recipe = new Recipe();
        recipe.mUUID = recipeCursor.getString(recipeCursor.getColumnIndex(Recipe.COLUMN.ID));
        recipe.mName = recipeCursor.getString(recipeCursor.getColumnIndex(Recipe.COLUMN.NAME));

        recipeCursor.close();
        return recipe;
    }

    public List<Ingredient> getIngredients(@NonNull String _recipeUUID) {
        List<Ingredient> ingredients = new ArrayList<>();
        Cursor cursor = mResolver.query(Uri.parse(RecipeProvider.MULTIPLE_RECIPE_INGREDIENT_CONTENT_URI.replace("*", _recipeUUID)),
                Ingredient.COLUMN.ALL_COLUMNS,
                null,
                null,
                null
        );
        if (cursor == null || cursor.getCount() == 0) {
            if (cursor != null) {
                cursor.close();
            }
            return ingredients;
        }
        cursor.moveToFirst();
        do {
            ingredients.add(parse(cursor));
        } while (cursor.moveToNext());

        return ingredients;
    }

    /**
     * Parse the Ingredient from an given cursor. Does not close the cursor
     *
     * @param _cursor the cursor with the ingredient info
     * @return the ingredient or null if no ingredient was found.
     */
    public Ingredient parse(@NonNull Cursor _cursor) {
        Ingredient ingredient = new Ingredient();
        ingredient.mUUID = _cursor.getString(_cursor.getColumnIndex(Ingredient.COLUMN.ID));
        ingredient.mAmount = _cursor.getFloat(_cursor.getColumnIndex(Ingredient.COLUMN.AMOUNT));
        ingredient.mProduct = mProductController.findById(_cursor.getString(_cursor.getColumnIndex(Ingredient.COLUMN.PRODUCT_ID)));
        ingredient.mRecipe = this.findById(_cursor.getString(_cursor.getColumnIndex(Ingredient.COLUMN.RECIPE_ID)));

        if (ingredient.mProduct == null || ingredient.mRecipe == null) {
            Log.i(LOG_TAG, "AddOrChangeIngredient: No product or recipe can be found with the given ids.");
            return null;
        }
        return ingredient;
    }

    @Override
    public Ingredient findIngredientById(String _uuid) {
        Cursor cursor = mResolver.query(Uri.parse(IngredientProvider.SINGLE_INGREDIENT_CONTENT_URI.replace("*", _uuid)),
                Ingredient.COLUMN.ALL_COLUMNS,
                null,
                null,
                null
        );
        if (cursor == null) {
            return null;
        }
        if (cursor.getCount() == 0) {
            Log.e(LOG_TAG, "No ingredient with id: " + _uuid + " found.");
            cursor.close();
            return null;
        }
        cursor.moveToFirst();

        Ingredient ingredient = parse(cursor);
        cursor.close();
        return ingredient;
    }

    @Override
    public List<Recipe> listAll() {
        Cursor recipeCursor = mResolver.query(Uri.parse(RecipeProvider.MULTIPLE_RECIPE_CONTENT_URI),
                Recipe.COLUMN.ALL_COLUMNS,
                null,
                null,
                null);
        List<Recipe> recipes = new ArrayList<>();
        if (recipeCursor == null) {
            return recipes;
        }

        recipeCursor.moveToFirst();
        if(recipeCursor.getCount() == 0){
            recipeCursor.close();
            return recipes;
        }
        do {
            recipes.add(parseRecipe(recipeCursor));
        } while (recipeCursor.moveToNext());
        recipeCursor.close();
        return recipes;
    }

    private Recipe parseRecipe(Cursor _cursor) {
        Recipe recipe = new Recipe();
        recipe.mUUID = _cursor.getString(_cursor.getColumnIndex(Recipe.COLUMN.ID));
        recipe.mName = _cursor.getString(_cursor.getColumnIndex(Recipe.COLUMN.NAME));
        return recipe;
    }
}
