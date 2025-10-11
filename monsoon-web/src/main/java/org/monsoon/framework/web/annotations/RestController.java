package org.monsoon.framework.web.annotations;

import org.monsoon.framework.core.annotations.Component;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface RestController {
    String value() default "";
}
