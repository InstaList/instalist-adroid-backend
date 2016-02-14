package org.noorganization.instalist.presenter.event;

import org.noorganization.instalist.model.ShoppingList;

/**
 * Event for notifying about changes of lists. Fired by the presenter. If a list got moved into
 * another category, the change is set to {@link Change#CHANGED}.
 * Created by daMihe on 21.07.2015.
 */
public class ListChangedMessage {
    public Change       mChange;
    public ShoppingList mList;

    public ListChangedMessage(Change _change, ShoppingList _list) {
        mChange = _change;
        mList   = _list;
    }
}
