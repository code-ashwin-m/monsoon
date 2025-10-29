package org.monsoon.framework.core.annotations;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoConfigureAfter {
    Class<?>[] value() default {};
    String[] name() default {};
}
