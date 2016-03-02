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

import android.database.Cursor;
import android.support.annotation.NonNull;

import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.Tag;
import org.noorganization.instalist.model.TaggedProduct;
import org.noorganization.instalist.model.Unit;

import java.util.List;

/**
 * The interface for modifying Products (created by software engineering). From view part, do only
 * modify data over this interface for keeping integrity.
 * Created by michi on 27.04.2015.
 */
public interface IProductController {

    /**
     * Creates a new product.
     *
     * @param _name          The title of the Product, null is not allowed.
     * @param _unit          A valid {@link org.noorganization.instalist.model.Unit} or null, if the Product
     *                       should not have a unit.
     * @param _defaultAmount The amount to propose when adding the product to the current list. A
     *                       valid number bigger than 0.0f.
     * @param _stepAmount    The amount to change when user hits "plus" or "minus" button for the
     *                       concrete {@link org.noorganization.instalist.model.ListEntry}. Must be a
     *                       positive number.
     * @return Either the created product, if something did not work.
     */
    Product createProduct(String _name, Unit _unit, float _defaultAmount, float _stepAmount);

    /**
     * Searches a product by UUID.
     *
     * @param _uuid The UUID to search for. Not Null.
     * @return Either the found product or null if not found or something went wrong.
     */
    Product findById(@NonNull String _uuid);

    /**
     * Changes a product.
     *
     * @param _toChange A valid product. See {@link #createProduct(String, org.noorganization.instalist.model.Unit, float, float)}
     *                  for more details about valid values. Not null.
     * @return Either the changed Product, the Product that was last time successfully saved (in
     * case something is wrong with the parameter) or null if Product was not found.
     */
    Product modifyProduct(Product _toChange);

    /**
     * Deletes the given Product.
     *
     * @param _toRemove         The valid Product to remove. Not null.
     * @param _deleteCompletely Whether to delete the Product also from lists etc. if used, or
     *                          delete anything if still used (and return in that case false). If
     *                          the product is not used, it will always get deleted.
     * @return Whether product was deleted.
     */
    boolean removeProduct(Product _toRemove, boolean _deleteCompletely);


    /**
     * Adds a tag to the product, if not already added.
     *
     * @param _product The valid product, not null.
     * @param _tag     The valid tag, not null.
     * @return If saving worked.
     */
    TaggedProduct addTagToProduct(Product _product, Tag _tag);

    /**
     * Removes a tag from the product, if tagged.
     *
     * @param _product The valid product, not null.
     * @param _tag     The valid tag, not null.
     */
    void removeTagFromProduct(Product _product, Tag _tag);

    List<TaggedProduct> findTaggedProductsByProduct(Product _product);

    Product parseProduct(Cursor _cursor);

    List<Product> listAll();
}
