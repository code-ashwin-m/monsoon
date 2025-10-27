package org.monsoon.framework.core;

import org.monsoon.framework.core.utils.ClassUtils;
import org.monsoon.framework.core.context.ApplicationContextFromConfigClass;
import org.monsoon.framework.core.interfaces.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the entry point to the Monsoon framework. It is responsible for creating the application context, which contains the bean definitions and the singletons.
 * To use the Monsoon framework, you should create an instance of this class and call the run method, passing the main class of the application as a parameter.
 * The main class should be annotated with @Configuration or @ComponentScan, so that the Monsoon framework can scan for the components and create the application context.
 */

public class Monsoon {
    private static final Logger logger = LoggerFactory.getLogger(Monsoon.class);
    private static Monsoon instance = null;
    private static Class<?> mainClass = null;
    private static final String CLASSPATH_WEB_CONTEXT = "org.monsoon.framework.web.context.ApplicationContextFromWebClass";
    private static ApplicationContext context = null;
    /**
     * This constructor is used to create an instance of the MonsoonApplication class.
     * @param clazz The main class of the application, which should be annotated with @Configuration or @ComponentScan.
     */
    public Monsoon(Class<?> clazz) {
        this.mainClass = clazz;
    }

    /**
     * This method is the entry point to the Monsoon framework. It should be called from the main method of the application.
     * @param clazz The main class of the application, which should be annotated with @Configuration or @ComponentScan.
     * @param args The command line arguments of the application.
     * @return The application context, which contains the bean definitions and the singletons.
     * @throws Exception If there is an error while creating the application context.
     */
    public static ApplicationContext run(Class<?> clazz, String[] args) throws Exception {
        logger.debug("Monsoon is running, main class is at {}", clazz.getName());
        instance = new Monsoon(clazz);

        // Create the application context from the configuration class
        context = createApplicationContext();
        return context;
    }

    /**
     * This method creates an application context from the given configuration class.
     * @return The application context, which contains the bean definitions and the singletons.
     * @throws Exception If there is an error while creating the application context.
     */
    private static ApplicationContext createApplicationContext() throws Exception {
        // Create the application context from the configuration class
        ApplicationContext context = null;
        if (ClassUtils.isPresent(CLASSPATH_WEB_CONTEXT)){
            logger.debug("Creating application context with web configuration");
            Class<?> webContextClass = Class.forName(CLASSPATH_WEB_CONTEXT);
            Object[] params = new Object[]{mainClass};

            context = (ApplicationContext) webContextClass.getDeclaredConstructors()[0].newInstance(params);
        }else {
            logger.debug("Creating application context with cli configuration");
            context = new ApplicationContextFromConfigClass(mainClass);
        }

        logger.debug("Context created with class {}", mainClass.getSimpleName());
        return context;
    }

    public static Monsoon getInstance() {
        return instance;
    }

    public static ApplicationContext getContext() {
        return context;
    }
}
