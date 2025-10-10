package org.monsoon.framework.core;

import org.monsoon.framework.core.annotations.MonsoonApplication;
import org.monsoon.framework.core.context.ApplicationContextFromConfigClass;
import org.monsoon.framework.core.context.ApplicationContext;

public class Monsoon {
    private Class<?> mainClass;
    private ApplicationContext context;
    private static Monsoon instance = null;

    public Monsoon(Class<?> mainClass) {
        this.mainClass = mainClass;
    }

    public static ApplicationContext run(Class<?> mainClass, String... args) throws Exception {
        if (mainClass.isAnnotationPresent(MonsoonApplication.class)){
            System.out.println("Monsoon Application");
            instance = new Monsoon(mainClass);
            return instance.createApplicationContext();
        } else {
            throw new IllegalArgumentException("Main class is not a Monsoon Application");
        }
    }

    private ApplicationContext createApplicationContext() throws Exception {
        ApplicationContext context = new ApplicationContextFromConfigClass(mainClass);
        this.context = context;
        return context;
    }

    public static Monsoon getInstance(){
        return instance;
    }

    public ApplicationContext getContext(){
        return context;
    }
}
