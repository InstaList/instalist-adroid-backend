package org.noorganization.instalist.presenter.event;

import org.noorganization.instalist.model.Category;

/**
 * Event for notifying about changes of categories. Fired by the presenter. Note that for Lists
 * that got added to or whose category changed, a {@link ListChangedMessage} will be fired.
 * Created by daMihe on 21.07.2015.
 */
public class CategoryChangedMessage {
    public Change   mChange;
    public Category mCategory;

    public CategoryChangedMessage(Change _change, Category _category) {
        mChange   = _change;
        mCategory = _category;
    }
}
