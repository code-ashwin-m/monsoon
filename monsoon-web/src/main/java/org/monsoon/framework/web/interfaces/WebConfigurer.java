package org.monsoon.framework.web.interfaces;

import org.monsoon.framework.web.FilterRegistration;

import java.util.List;

public interface WebConfigurer {
    default void addInterceptors(List<HandlerInterceptor> registry){

    }
    default void addFilters(List<FilterRegistration> registry){

    }
}
