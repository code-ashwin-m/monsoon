package org.monsoon.framework.web.context;

import org.monsoon.framework.core.BeanDefinition;
import org.monsoon.framework.core.context.ApplicationContextHelper;
import org.monsoon.framework.core.interfaces.ApplicationContext;
import org.monsoon.framework.core.properties.ApplicationProperties;
import org.monsoon.framework.web.EmbeddedWebServer;
import org.monsoon.framework.web.annotations.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * ApplicationContextFromWebClass is a class that extends ApplicationContextHelper
 * and implements the ApplicationContext interface. It is used to create an
 * application context for a web application.
 */
public class ApplicationContextFromWebClass extends ApplicationContextHelper implements ApplicationContext {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationContextFromWebClass.class);
    private final List<Object> restControllers = new ArrayList<>();

    /**
     * Creates an instance of the ApplicationContextFromWebClass class.
     *
     * @param mainClass the main class of the application
     * @throws Exception if an error occurs while creating the application context
     */
    public ApplicationContextFromWebClass(Class<?> mainClass) throws Exception {
        super(mainClass);
    }

    /**
     * Returns the bean with the specified name.
     *
     * @param beanName the name of the bean to return
     * @return the bean with the specified name
     * @throws Exception if an error occurs while getting the bean
     */
    @Override
    public Object getBean(String beanName) throws Exception {
        return createBean(beanName);
    }

    /**
     * Returns the bean with the specified name and class.
     *
     * @param beanName the name of the bean to return
     * @param clazz the class of the bean to return
     * @return the bean with the specified name and class
     * @throws Exception if an error occurs while getting the bean
     */
    @Override
    public <T> T getBean(String beanName, Class<T> clazz) throws Exception {
        return clazz.cast(createBean(beanName));
    }

    @Override
    public Object getBeanOrNull(String beanName) {
        try {
            return createBean(beanName);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public <T> T getBeanOrNull(String beanName, Class<T> clazz) {
        try {
            return clazz.cast(createBean(beanName));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Refreshes the application context.
     *
     * @throws Exception if an error occurs while refreshing the application context
     */
    @Override
    public void refresh() throws Exception {
        for (BeanDefinition def: beanDefinitions.values()){
            if (def.getBeanClass().isAnnotationPresent(RestController.class)){
                restControllers.add(createBean(def.getBeanName()));
            }
        }
        startServer();
    }

    /**
     * Starts the server.
     *
     * @throws Exception if an error occurs while starting the server
     */
    private void startServer() throws Exception {
        logger.debug("Starting local server");
        String host = ApplicationProperties.get("server.host", "http://localhost");
        if (!host.startsWith("http://") && !host.startsWith("https://")) host = "http://" + host;
        Integer port = Integer.parseInt(ApplicationProperties.get("server.port", "8080"));

        EmbeddedWebServer server = new EmbeddedWebServer();
        for (Object controller: restControllers){
            server.registerController(controller);
        }
        server.start(port);
        logger.debug("Server started at {}:{}", host, port);
    }
}
