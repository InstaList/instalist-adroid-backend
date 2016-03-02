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
