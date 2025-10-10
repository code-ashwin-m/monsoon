package org.monsoon.framework.db.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    String name();
    boolean unique() default false;
    boolean uniqueCombo() default false;
}
