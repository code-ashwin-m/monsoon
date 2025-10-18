package org.monsoon.framework.web;

import org.monsoon.framework.core.annotations.Component;
import org.monsoon.framework.core.context.ApplicationContext;
import org.monsoon.framework.core.context.ApplicationContextHelper;
import org.monsoon.framework.core.context.BeanDefinition;
import org.monsoon.framework.web.annotations.RestController;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.List;

public class ApplicationContextFromWeb extends ApplicationContextHelper implements ApplicationContext {
    private final List<Object> restControllers = new ArrayList<>();
    public ApplicationContextFromWeb(Class<?> mainClass) throws Exception {
        super(mainClass);
    }

    @Override
    public <T> T getBean(String beanName, Class<T> clazz) throws Exception {
        return clazz.cast(getBean(beanName));
    }

    @Override
    public <T> T getBean(Class<T> clazz) throws Exception {
        Component component = findAnnotation(clazz, Component.class);
        String beanName = component.name();
        if (beanName.equals("")) beanName = Introspector.decapitalize(clazz.getSimpleName());
        return clazz.cast(getBean(beanName));
    }

    @Override
    public Object getBean(String beanName) throws Exception {
        BeanDefinition def = beanDefinitions.get(beanName);
        if (def == null){
            throw new IllegalArgumentException("Bean not found: " + beanName);
        }
        if (def.isSingleton()){
            if (singletonBeans.containsKey(beanName)) return singletonBeans.get(beanName);
            Object instance = createInstance(def.getBeanClass());
            singletonBeans.put(beanName, instance);
            return instance;
        }
        return createInstance(def.getBeanClass());
    }

    @Override
    public Object refresh() throws Exception {
        System.out.println("Starting web server");
        for (BeanDefinition def : beanDefinitions.values()) {
            if (def.getBeanClass().isAnnotationPresent(RestController.class)) {
                restControllers.add(getBean(def.getBeanClass()));
            }
        }
        return startServer();
    }

    private Object startServer() throws Exception {
        if (isRunningInsideServletContainer()) {
            System.out.println("Detected tomcat server, running app from container.");
            ServletWebAdapter server = new ServletWebAdapter(this);
            for (Object controller: restControllers){
                server.registerController(controller);
            }
            return server;
        } else {
            System.out.println("Running app from local server");
            EmbeddedWebServer server = new EmbeddedWebServer();
            for (Object controller: restControllers){
                server.registerController(controller);
            }
            server.start(8080);
            return server;
        }
    }

    private boolean isRunningInsideServletContainer() {
        try {
            Class.forName("javax.servlet.ServletContext");
            return System.getProperty("catalina.base") != null; // Tomcat-specific hint
        } catch (Exception e) {
            return false;
        }
    }
}
