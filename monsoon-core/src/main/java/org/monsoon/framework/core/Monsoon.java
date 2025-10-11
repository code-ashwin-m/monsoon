package org.monsoon.framework.core;

import org.monsoon.framework.core.annotations.MonsoonApplication;
import org.monsoon.framework.core.context.ApplicationContextFromConfigClass;
import org.monsoon.framework.core.context.ApplicationContext;
import org.monsoon.framework.core.utils.ClassUtils;

public class Monsoon {
    private Class<?> mainClass;
    private ApplicationContext context;
    private static Monsoon instance = null;
    private static final String CLASSPATH_WEB_CONTEXT = "org.monsoon.framework.web.ApplicationContextFromWeb";

    public Monsoon(Class<?> mainClass) {
        this.mainClass = mainClass;
    }

    public static ApplicationContext run(Class<?> mainClass, String... args) throws Exception {
        if (mainClass.isAnnotationPresent(MonsoonApplication.class)){
            System.out.println("Monsoon Application");
            instance = new Monsoon(mainClass);
            ApplicationContext context = instance.createApplicationContext();
            return context;
        } else {
            throw new IllegalArgumentException("Main class is not a Monsoon Application");
        }
    }

    private ApplicationContext createApplicationContext() throws Exception {
        ApplicationContext context;
        if (ClassUtils.isPresent(CLASSPATH_WEB_CONTEXT)){
            System.out.println("Web Context");
            Class<?> webContextClass = Class.forName(CLASSPATH_WEB_CONTEXT);
            Object[] params = new Object[]{mainClass};

            context = (ApplicationContext) webContextClass.getDeclaredConstructors()[0].newInstance(params);
        }else{
            System.out.println("CLI Context");
            context = new ApplicationContextFromConfigClass(mainClass);
        }

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
