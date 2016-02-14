package org.noorganization.instalist.presenter.event;

import org.noorganization.instalist.model.Recipe;

/**
 * Event class for notifying about changes in recipes.
 * Created by daMihe on 05.07.2015.
 */
public class RecipeChangedMessage {
    public Recipe mRecipe;
    public Change mChange;

    public RecipeChangedMessage(Change _change, Recipe _recipe) {
        mRecipe = _recipe;
        mChange = _change;
    }
}
