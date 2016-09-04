package org.noorganization.instalist.types;

import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The possible controller types.
 *
 * @author Desnoo
 */
public class ControllerType {
    public static final int CATEGORY = 0;
    public static final int LIST = 1;
    public static final int LIST_ENTRY = 2;
    public static final int INGREDIENT = 3;
    public static final int PRODUCT = 4;
    public static final int RECIPE = 5;
    public static final int TAG = 6;
    public static final int TAGGED_PRODUCT = 7;
    public static final int UNIT = 8;
    public static final int ERROR_LOG = 9;
    public static final int MODEL_MAPPING = 10;
    public static final int ALL = 11;

    @IntDef({CATEGORY, LIST, LIST_ENTRY, INGREDIENT, PRODUCT, RECIPE, TAG, TAGGED_PRODUCT, UNIT, ERROR_LOG, MODEL_MAPPING, ALL})
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE})
    public @interface Controller {
    }
}
