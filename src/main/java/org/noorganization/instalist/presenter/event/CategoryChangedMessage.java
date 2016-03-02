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
