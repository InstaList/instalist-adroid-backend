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

package org.noorganization.instalist.presenter;

import android.support.annotation.NonNull;

import org.noorganization.instalist.model.Category;

import java.util.List;

/**
 * The presenter for creating and changing lists categories. Use this interface to save integrity.
 * Created by daMihe on 25.05.2015.
 */
public interface ICategoryController {

    /**
     * Creates a category.
     * @param _name The name of the new category, must not be already used. Not null or empty.
     * @return The new category if creating worked, else null.
     */
    Category createCategory(String _name);

    /**
     * Collects all categories.
     * @return A List of all categories (maybe empty). If something fails, null will be returned.
     */
    List<Category> getAllCategories();

    /**
     * Searches a category by uuid.
     * @param _uuid The categories UUID. Not null.
     * @return Either a found and parsed Category or null, if not found.
     */
    Category getCategoryByID(@NonNull String _uuid);

    /**
     * Returns the count of categories.
     * @return The amount of categories.
     */
    int getCategoryCount();

    /**
     * Renames a category.
     * @param _toRename The saved Category to change (created by {@link #createCategory(String)}.
     *                  Not null.
     * @param _newName The new name of the category, not null, empty or already used by another
     *                 Category.
     * @return The changed category if everything was OK. In other cases either the old category or
     * null if category to change was not found.
     */
    Category renameCategory(Category _toRename, String _newName);

    void removeCategory(Category _toRemove);
}
