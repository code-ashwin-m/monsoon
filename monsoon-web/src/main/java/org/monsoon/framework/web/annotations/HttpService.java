package org.monsoon.framework.web.annotations;

import org.monsoon.framework.core.annotations.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpService {
    String baseUrl();
}
