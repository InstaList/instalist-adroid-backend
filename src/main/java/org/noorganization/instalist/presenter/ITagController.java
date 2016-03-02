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

import android.content.Context;

import org.noorganization.instalist.model.Tag;
import org.noorganization.instalist.presenter.implementation.ControllerFactory;

import java.util.List;

/**
 * The interface to create, change and remove tags for products. This interface was created via
 * software engineering, it's current implementation can be accessed with
 * {@link ControllerFactory#getTagController(Context)} )}.
 * <p/>
 * Use this interface to ensure integrity.
 * Created by daMihe on 11.05.2015.
 */
public interface ITagController {

    /**
     * Create a Tag with given title.
     *
     * @param _title The new title of the tag. Not null. Should not exist as a tag.
     * @return Either the created tag or null, if title was invalid or is already used.
     */
    Tag createTag(String _title);

    /**
     * Rename a Tag.
     *
     * @param _toRename The valid (created through {@link #createTag(String)}) Tag. Not null.
     * @param _newTitle A new title. Same requirements like when new creating a new tag: Not null,
     *                  should not be already used.
     * @return Either the modified tag if everything was ok. Null, if the tag to rename was not
     * found or the old tag if _newTitle was wrong.
     */
    Tag renameTag(Tag _toRename, String _newTitle);

    /**
     * Removes a tag and all references.
     *
     * @param _toRemove The valid (created through {@link #createTag(String)}) Tag. Not null.
     */
    void removeTag(Tag _toRemove);

    /**
     * Find a {@link Tag} by an id.
     *
     * @param _uuid the uuid of an tag.
     * @return the tag itself or null if none was found.
     */
    Tag findById(String _uuid);

    Tag findByName(String _name);

    /**
     * A list of all tags.
     *
     * @return an list consisting of all tags.
     */
    List<Tag> listAll();
}
