package org.monsoon.framework.web.annotations;

import org.monsoon.framework.core.annotations.Component;
import org.monsoon.framework.core.annotations.Controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Controller
@ResponseBody
public @interface RestController {
    String value() default "";
}
