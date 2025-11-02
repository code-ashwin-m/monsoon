package org.monsoon.sample;

import org.monsoon.framework.core.annotations.Autowired;
import org.monsoon.framework.core.annotations.Configuration;
import org.monsoon.framework.web.FilterRegistration;
import org.monsoon.framework.web.interfaces.HandlerInterceptor;
import org.monsoon.framework.web.interfaces.WebConfigurer;

import java.util.List;

@Configuration
public class TestWebConfig implements WebConfigurer {
    @Autowired
    private TestInterceptor testInterceptor;

    @Autowired
    private TestFilter testFilter;

    @Override
    public void addInterceptors(List<HandlerInterceptor> registry) {
        registry.add(testInterceptor);
    }

    @Override
    public void addFilters(List<FilterRegistration> registry) {
        registry.add(new FilterRegistration(testFilter, TestFilter.class, 0, "/*"));
    }
}
