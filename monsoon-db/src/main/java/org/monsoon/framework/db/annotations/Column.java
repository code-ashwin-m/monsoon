package org.monsoon.framework.db.annotations;

import org.monsoon.framework.db.datapersister.DefaultDataPersister;
import org.monsoon.framework.db.interfaces.DataPersister;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    String name() default "";
    boolean unique() default false;
    boolean uniqueCombo() default false;
    Class<? extends DataPersister<?>> convertor() default DefaultDataPersister.class;
}
