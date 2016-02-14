package org.noorganization.instalist.presenter;

import android.support.annotation.NonNull;

import org.noorganization.instalist.model.Unit;

import java.util.List;

/**
 * The interface for modifying Units (created by software engineering). From view part, do only
 * modify data over this interface for keeping integrity.
 * Created by michi on 03.05.2015.
 */
public interface IUnitController {
    int MODE_BREAK_DELETION = 0;
    int MODE_UNLINK_REFERENCES = 1;
    int MODE_DELETE_REFERENCES = 2;

    /**
     * Creates a Unit with given name or fails, if it already exists.
     * @param _name The name of the new Unit. Not null.
     * @return The new Unit or null, if the parameter is invalid or a Unit with that name already
     * exists.
     */
    Unit createUnit(String _name);

    /**
     * Searches a unit by UUID.
     * @param _uuid the id of the searched {@link Unit}.
     * @return null if no unit was found else the unit.
     */
    Unit findById(@NonNull String _uuid);

    /**
     * Find the Unit by its name.
     * @param _name the name to search.
     * @return the related unit or null if nothing was found.
     */
    Unit findByName(String _name);

        /**
         * Rename the Unit, if possible.
         * @param _unit The valid unit to rename. Not null.
         * @param _newName The new name of the unit. Not null. Not existent.
         * @return The modified Unit if everything went ok, the last saved unit if renaming was not
         * possible or null if the unit could not be found.
         */
    Unit renameUnit(Unit _unit, String _newName);

    /**
     * Deletes a unit. Deletion can be controlled with the mode parameter.
     * @param _unit The valid unit to delete. Not null.
     * @param _mode Mode for handling conflicts. Must be one of {@link #MODE_BREAK_DELETION},
     *              {@link #MODE_DELETE_REFERENCES} or {@link #MODE_UNLINK_REFERENCES}.
     * @return False if nothing was deleted (can happen if mode is set to BREAK_DELETION) or mode is
     * invalid, else true. Also true if unit was already deleted.
     */
    boolean deleteUnit(Unit _unit, int _mode);

    List<Unit> listAll(String _orderByColumn, boolean _asc);

    Unit getDefaultUnit();
}
