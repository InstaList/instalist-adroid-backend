package org.noorganization.instalist.presenter;

import android.support.annotation.NonNull;

import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.model.ListEntry;
import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.ShoppingList;

import java.util.List;

/**
 * IListController is the (by software engineering created) interface for modifying
 * shopping lists. If there is no documentation for the methods, following is always valid:
 * - for parameters, null's are not allowed. If set to null, no modification will be made and false
 *   will be returned.
 * - it will be always returned whether the modification worked.
 * Created by michi on 20.04.2015.
 */
public interface IListController {

    /**
     * Shortcut for {@link #addOrChangeItem(ShoppingList, Product, float, int)} with priority set to
     * 0 and replacing amount of an existing item.
     */
    ListEntry addOrChangeItem(ShoppingList _list, Product _product, float _amount);

    /**
     * Shortcut for {@link #addOrChangeItem(ShoppingList, Product, float, int, boolean)}, replacing
     * amount of an existing item.
     */
    ListEntry addOrChangeItem(ShoppingList _list, Product _product, float _amount, int _prio);

    /**
     * Shortcut for {@link #addOrChangeItem(ShoppingList, Product, float, int, boolean)}, not
     * updating any priority (if creating, then set to 0)
     */
    ListEntry addOrChangeItem(ShoppingList _list, Product _product, float _amount,
                              boolean _addAmount);

    /**
     *
     * Adds or updates an item to an existing list. The amount will be updated (not added) if the
     * corresponding ListEntry already exists.
     * @param _list A valid ShoppingList, not null.
     * @param _product A valid Product, not null.
     * @param _amount The amount of the product. Not +infty, NaN or lesser than 0.001f.
     * @param _prio  The priority of the ListEntry. 0 does mean neutral, higher values mean higher
     *               priority.
     * @param _addAmount Whether to add the amount if ListEntry exits (= true) or to replace it
     *                   (= false).
     * @return The created or updated ListEntry. If not worked, null or the old item will be
     * returned.
     */
    ListEntry addOrChangeItem(ShoppingList _list, Product _product, float _amount, int _prio,
                              boolean _addAmount);

    List<ShoppingList> getAllLists();

    /**
     * Searches a ListEntry.
     * @param _UUID The UUID identifying the entry.
     * @return The found ListEntry or null if something went wrong or not found.
     */
    ListEntry getEntryById(@NonNull String _UUID);

    /**
     * Searchess a ListEntry.
     * @param _list The list as parameter for the search. Not null.
     * @param _product The product as parameter for the search. Not null.
     * @return Either the found entry or null if nothing found or something went wrong.
     */
    ListEntry getEntryByListAndProduct(@NonNull ShoppingList _list, @NonNull Product _product);

    int getEntryCount(@NonNull ShoppingList _list);

    /**
     * Searches a ShoppingList.
     * @param _UUID The UUID identifying the list.
     * @return The found ShoppingList or null if something went wrong or not found.
     */
    ShoppingList getListById(@NonNull String _UUID);

    /**
     * Searches ShoppingLists with given ids.
     * @param _category The category, to use as parameter. If null, lists will be searched, which
     *                  don't belong to a category.
     * @return Either a list with results (may be also empty) or null if category does not exist (if
     * set) or an error occurs.
     */
    List<ShoppingList> getListsByCategory(Category _category);

    /**
     * Strikes all items on a list.
     * @param _list The valid shopping list to strike. Not null.
     */
    void strikeAllItems(ShoppingList _list);

    /**
     * Unstrikes all items on a list.
     * @param _list The valid shopping list to strike. Not null.
     */
    void unstrikeAllItems(ShoppingList _list);

    /**
     * Strikes a ListEntry.
     * @param _list A valid ShoppingList, not null.
     * @param _product A valid Product, not null.
     * @return The changed item or null, if not found or parameters invalid.
     */
    ListEntry strikeItem(ShoppingList _list, Product _product);

    /**
     * Removes the virtual stroke from a ListEntry.
     * @param _list A valid ShoppingList, not null.
     * @param _product A valid Product, not null.
     * @return The changed item or null, if not found or parameters invalid.
     */
    ListEntry unstrikeItem(ShoppingList _list, Product _product);

    /**
     * Alias for {@link #strikeItem(org.noorganization.instalist.model.ShoppingList, org.noorganization.instalist.model.Product)}.
     * @param _item A valid ListEntry, not null.
     * @return The changed item or null, if _item was invalid.
     */
    ListEntry strikeItem(ListEntry _item);

    /**
     * Alias for {@link #unstrikeItem(org.noorganization.instalist.model.ShoppingList, org.noorganization.instalist.model.Product)}.
     * @param _item A valid ListEntry, not null.
     * @return The changed item or null if _item was invalid.
     */
    ListEntry unstrikeItem(ListEntry _item);

    boolean removeItem(ShoppingList _list, Product _product);
    boolean removeItem(ListEntry _item);

    /**
     * Set a new priority for an item.
     * @param _item The item to priorize, not null, saved.
     * @param _newPrio A new priority (0 = neutral, less = lower priority, more = higher priority).
     * @return The changed ListEntry or null if not found.
     */
    ListEntry setItemPriority(ListEntry _item, int _newPrio);

    /**
     * Creates a list and returns it.
     * @param _name The name of the new list. Not null, not empty.
     * @return The new list or null, if creation failed.
     */
    ShoppingList addList(String _name);

    /**
     * Creates a list in a specific category.
     * @param _name The name of the category. Not null, not empty.
     * @param _category The specific, already saved category. Null is possible if no category.
     * @return The created list or null if creation failed.
     */
    ShoppingList addList(String _name, Category _category);
    boolean removeList(ShoppingList _list);

    /**
     * Renames a list and returns it. The name must be unique or the rename will fail.
     * @param _list A valid ShoppingList, not null.
     * @param _newName A new name for the list. Not null, not empty.
     * @return The modified list or the old list.
     */
    ShoppingList renameList(ShoppingList _list, String _newName);

    /**
     * Moves a list to a category.
     * @param _list The list to move (saved, not null).
     * @param _category The saved category or null, if no category. If category can't be found and
     *                  is not null, moving fails.
     * @return The changed ShoppingList or null if moving failed.
     */
    ShoppingList moveToCategory(ShoppingList _list, Category _category);

    /**
     * List all entries of a given shoppinglist
     * @param _shoppingListUUID the uuid of the shoppingList
     * @param _categoryUUID
     * @return an empty list or a filled list with corresponding {@link ListEntry}s. Or null if an error occured.
     */
    List<ListEntry> listAllListEntries(String _shoppingListUUID, String _categoryUUID);
}
