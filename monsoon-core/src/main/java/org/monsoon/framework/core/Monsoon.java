package org.monsoon.framework.core;

import org.monsoon.framework.core.configuration.MonsoonApplication;
import org.monsoon.framework.core.context.AnnotationConfigApplicationContext;
import org.monsoon.framework.core.context.ApplicationContext;
import org.monsoon.framework.core.context.Context;

public class Monsoon {
    private Class<?> mainClass;
    private Context context;

    public Monsoon(Class<?> mainClass) {
        this.mainClass = mainClass;
    }

    public static ApplicationContext run(Class<?> mainClass, String... args) throws Exception {
        if (mainClass.isAnnotationPresent(MonsoonApplication.class)){
            System.out.println("Monsoon Application");
            return new Monsoon(mainClass).createApplicationContext();
        } else {
            throw new IllegalArgumentException("Main class is not a Monsoon Application");
        }
    }

    private ApplicationContext createApplicationContext() throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext(mainClass);
        this.context = context;
        return context;
    }
}
