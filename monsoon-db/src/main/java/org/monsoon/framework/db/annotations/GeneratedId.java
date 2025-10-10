package org.monsoon.framework.db.annotations;

import org.monsoon.framework.db.DefaultIdGenerator;
import org.monsoon.framework.db.interfaces.IdGenerator;
import org.monsoon.framework.db.enums.GenerationType;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GeneratedId {
    GenerationType strategy() default GenerationType.AUTO;
    Class<? extends IdGenerator> generator() default DefaultIdGenerator.class;
}