package org.noorganization.instalist.enums;

/**
 * The type of the model.
 * Created by Desnoo on 16.02.2016.
 */
public enum eModelType {
    CATEGORY,
    LIST,
    LIST_ENTRY,
    INGREDIENT,
    PRODUCT,
    RECIPE,
    TAG,
    TAGGED_PRODUCT,
    UNIT,
    ALL;

    public static eModelType getTypeId(int _model) {
        switch (_model) {
            case 0:
                return CATEGORY;
            case 1:
                return LIST;
            case 2:
                return LIST_ENTRY;
            case 3:
                return INGREDIENT;
            case 4:
                return PRODUCT;
            case 5:
                return RECIPE;
            case 6:
                return TAG;
            case 7:
                return TAGGED_PRODUCT;
            case 8:
                return UNIT;
            default:
                return null;
        }
    }
}
