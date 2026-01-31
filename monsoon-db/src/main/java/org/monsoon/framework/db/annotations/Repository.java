package org.monsoon.framework.db.annotations;

import org.monsoon.framework.core.annotations.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Repository {
    String name() default "";
    Class<?> entity();
}
