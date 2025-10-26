package org.monsoon.framework.test;

import org.monsoon.framework.core.annotations.Component;

@Component( name = "testComp")
public class TestComponent {
    public String process(){
        return "Hello from bean";
    }
}
