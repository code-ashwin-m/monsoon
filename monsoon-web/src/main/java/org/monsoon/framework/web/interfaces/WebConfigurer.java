package org.monsoon.framework.web.interfaces;

import java.util.List;

public interface WebConfigurer {
    void addInterceptors(List<HandlerInterceptor> registry);
}
