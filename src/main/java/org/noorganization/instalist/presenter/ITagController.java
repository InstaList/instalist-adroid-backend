package org.noorganization.instalist.presenter;

import org.noorganization.instalist.presenter.implementation.ControllerFactory;
import org.noorganization.instalist.model.Tag;

/**
 * The interface to create, change and remove tags for products. This interface was created via
 * software engineering, it's current implementation can be accessed with
 * {@link ControllerFactory#getTagController()}.
 *
 * Use this interface to ensure integrity.
 * Created by daMihe on 11.05.2015.
 */
public interface ITagController {

    /**
     * Create a Tag with given title.
     * @param _title The new title of the tag. Not null. Should not exist as a tag.
     * @return Either the created tag or null, if title was invalid or is already used.
     */
    Tag createTag(String _title);

    /**
     * Rename a Tag.
     * @param _toRename The valid (created through {@link #createTag(String)}) Tag. Not null.
     * @param _newTitle A new title. Same requirements like when new creating a new tag: Not null,
     *                  should not be already used.
     * @return Either the modified tag if everything was ok. Null, if the tag to rename was not
     * found or the old tag if _newTitle was wrong.
     */
    Tag renameTag(Tag _toRename, String _newTitle);

    /**
     * Removes a tag and all references.
     * @param _toRemove The valid (created through {@link #createTag(String)}) Tag. Not null.
     */
    void removeTag(Tag _toRemove);

    /**
     * Find a {@link Tag} by an id.
     * @param _uuid the uuid of an tag.
     * @return the tag itself or null if none was found.
     */
    Tag findById(String _uuid);

    Tag findByName(String _name);
}
