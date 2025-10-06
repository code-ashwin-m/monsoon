package org.monsoon.framework.core.context;

public class AnnotationConfigApplicationContext extends ApplicationContext {
    public AnnotationConfigApplicationContext(Class<?> mainClass) throws Exception {
        super(mainClass);
    }
}
