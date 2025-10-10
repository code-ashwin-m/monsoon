package org.monsoon.test.coretest;

import org.monsoon.framework.core.annotations.Autowired;
import org.monsoon.framework.core.annotations.Component;

@Component
public class TestComponent {
    @Autowired
    private TestComponentSingleton singleton;

    public TestComponentSingleton getSingleton() {
        return singleton;
    }
}
