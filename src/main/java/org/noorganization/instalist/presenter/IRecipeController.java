package org.noorganization.instalist.presenter;

import android.support.annotation.NonNull;

import org.noorganization.instalist.model.Ingredient;
import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.Recipe;

import java.util.List;

/**
 * The interface to change recipes (created by software engneering). Use only this interface for
 * changing recipes and ingredients to ensure integrity.
 * Created by michi on 04.05.15.
 */
public interface IRecipeController {
    /**
     * Create a recipe with a new name.
     *
     * @param _name Not null. Should not exist in database.
     * @return The new recipe or null, if a recipe with the given name already exists or parameter
     * was invalid.
     */
    Recipe createRecipe(String _name);

    /**
     * Rename a recipe, when there is no other recipe with same new name.
     *
     * @param _toChange The valid recipe to change. Not null.
     * @param _newName  The new name. Should not exist already and mustn't be null.
     * @return The changed recipe if everything was ok. If name is already taken or
     * new name is invalid the old recipe. null if recipe was not found.
     */
    Recipe renameRecipe(Recipe _toChange, String _newName);

    /**
     * Remove a receipe and all it's ingredients.
     *
     * @param _toRemove The recipe to remove. Not null.
     */
    void removeRecipe(Recipe _toRemove);

    /**
     * Adds an ingredient to an existing recipe or update its amount.
     *
     * @param _recipe       The recipe where the product should be an ingredient. Not null.
     * @param _productToAdd The product that should be added or updated.
     * @param _amount       The (new) amount.
     * @return Either the newly created Ingredient, the old Ingredient (if already exists but given
     * amount is invalid) or null (if ingredient did not exist and given amount/product/recipe was
     * invalid).
     */
    Ingredient addOrChangeIngredient(Recipe _recipe, Product _productToAdd, float _amount);

    /**
     * Remove ingredient from recipe.
     *
     * @param _recipe          The recipe to change.
     * @param _productToRemove The product that should be removed.
     */
    void removeIngredient(Recipe _recipe, Product _productToRemove);

    void removeIngredient(Ingredient _toRemove);

    /**
     * Find a recipe by the uuid of it.
     *
     * @param _uuid the uuid of the recipe to find.
     * @return the recipe or null if not found.
     */
    Recipe findById(String _uuid);

    /**
     * Find a recipe by the name of it.
     *
     * @param _name the name of the recipe to find.
     * @return the recipe or null if not found.
     */
    Recipe findByName(@NonNull String _name);

    Ingredient findIngredientById(String _uuid);

    /**
     * Find a list of ingredients by a given recipe uuid.
     *
     * @param _recipeUUID the recipe uuid.
     * @return a list of ingredients.
     */
    List<Ingredient> getIngredients(@NonNull String _recipeUUID);

    List<Recipe> listAll();
}