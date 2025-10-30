package org.monsoon.framework.web.context;

import org.monsoon.framework.core.BeanDefinition;
import org.monsoon.framework.core.annotations.Controller;
import org.monsoon.framework.core.context.ApplicationContextHelper;
import org.monsoon.framework.core.interfaces.ApplicationContext;
import org.monsoon.framework.core.properties.ApplicationProperties;
import org.monsoon.framework.core.utils.ClassUtils;
import org.monsoon.framework.web.ServletWebAdapter;
import org.monsoon.framework.web.autoconfigure.DefaultServerAutoConfiguration;
import org.monsoon.framework.web.interfaces.EmbeddedServer;
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
    public Object refresh() throws Exception {
        for (BeanDefinition def: beanDefinitions.values()){
            if (ClassUtils.isAnnotationPresent(def.getBeanClass(), Controller.class)){
                restControllers.add(createBean(def.getBeanName()));
            }
        }
        return startServer();
    }

    @Override
    public void loadContext() {
        refreshContext();
    }

    /**
     * Starts the server.
     *
     * @throws Exception if an error occurs while starting the server
     */
    private ServletWebAdapter startServer() throws Exception {
        if (isRunningInsideServletContainer()) {
            logger.debug("Servlet container detected");
            ServletWebAdapter servletWebAdapter = new ServletWebAdapter();
            restControllers.forEach(servletWebAdapter::registerController);
            return servletWebAdapter;
        }

        EmbeddedServer embeddedServer = getBeanOrNull("embeddedServer", EmbeddedServer.class);
        if (embeddedServer instanceof DefaultServerAutoConfiguration.DefaultEmbeddedServer) {
            logger.error("Missing server dependency");
            embeddedServer.start(null, 0, null);
            return null;
        }

        String host = ApplicationProperties.get("server.host", "http://localhost");
        if (!host.startsWith("http://") && !host.startsWith("https://")) {
            host = "http://" + host;
        }

        ServletWebAdapter servletWebAdapter = new ServletWebAdapter();
        restControllers.forEach(servletWebAdapter::registerController);

        int port = Integer.parseInt(ApplicationProperties.get("server.port", "8080"));
        embeddedServer.start(host, port, servletWebAdapter);

        return servletWebAdapter;
    }

    /**
     * Checks if the application is running inside a servlet container.
     *
     * @return true if the application is running inside a servlet container, false otherwise
     */
    private boolean isRunningInsideServletContainer() {
        try {
            Class.forName("javax.servlet.ServletContext");
            return System.getProperty("catalina.base") != null; // Tomcat-specific hint
        } catch (Exception e) {
            return false;
        }
    }
}
