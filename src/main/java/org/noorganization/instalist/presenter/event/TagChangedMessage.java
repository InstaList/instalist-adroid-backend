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

import org.noorganization.instalist.model.Tag;

/**
 * Event for notifying about changes of tags. Fired by the presenter. Note: (Un-)Tagged products
 * will be notified with a {@link ProductChangedMessage}.
 * Created by daMihe on 21.07.2015.
 */
public class TagChangedMessage {
    public Change mChange;
    public Tag    mTag;

    public TagChangedMessage(Change _change, Tag _tag) {
        mChange = _change;
        mTag    = _tag;
    }
}
