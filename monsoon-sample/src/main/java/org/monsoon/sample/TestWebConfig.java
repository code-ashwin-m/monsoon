package org.monsoon.sample;

import org.monsoon.framework.core.annotations.Autowired;
import org.monsoon.framework.core.annotations.Configuration;
import org.monsoon.framework.web.interfaces.HandlerInterceptor;
import org.monsoon.framework.web.interfaces.WebConfigurer;

import java.util.List;

@Configuration
public class TestWebConfig implements WebConfigurer {
    @Autowired
    private TestInterceptor testInterceptor;

    @Override
    public void addInterceptors(List<HandlerInterceptor> registry) {
        registry.add(testInterceptor);
    }
}
