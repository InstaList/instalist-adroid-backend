package org.noorganization.instalist.enums;

/**
 * Action types.
 * Created by Desnoo on 16.02.2016.
 */
public enum eActionType {
    UPDATE,
    DELETE,
    INSERT,
    CONFLICT,
    ALL;

    public static eActionType getTypeById(int _actionId) {
        switch (_actionId) {
            case 0:
                return UPDATE;
            case 1:
                return DELETE;
            case 2:
                return INSERT;
            case 3:
                return CONFLICT;
            default:
                return null;
        }
    }
}
