package org.noorganization.instalist.types;

import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The type of an action.
 *
 * @author Desnoo
 */
public class ActionType {
    public static final int UPDATE = 0;
    public static final int DELETE = 1;
    public static final int INSERT = 2;
    public static final int CONFLICT = 3;
    public static final int ALL = 4;

    @IntDef({UPDATE, DELETE, INSERT, CONFLICT, ALL})
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE})
    public @interface Action {
    }

}
