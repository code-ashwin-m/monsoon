package org.monsoon.framework.core.context;

import org.monsoon.framework.core.interfaces.ApplicationContext;
import org.monsoon.framework.core.interfaces.BeanPostProcessor;

/**
 * This class is a factory for creating an application context from a configuration class.
 * It extends the ApplicationContextHelper class and implements the ApplicationContext interface.
 */
public class ApplicationContextFromConfigClass extends ApplicationContextHelper implements ApplicationContext {
    public ApplicationContextFromConfigClass(Class<?> mainClass) throws Exception {
        super(mainClass);
    }

    /**
     * This method returns a bean instance from the application context.
     * It looks up the bean definition in the application context and creates a bean instance.
     * If the bean definition is not found, it returns null.
     * The class is thread-safe and can be used in a multi-threaded environment.
     * @param beanName The name of the bean to return.
     * @return The bean instance, or null if the bean definition is not found.
     * @throws Exception If there is an error while returning the bean instance.
     */
    @Override
    public Object getBean(String beanName) throws Exception {
        return createBean(beanName);
    }

    /**
     * This method returns a bean instance from the application context.
     * It looks up the bean definition in the application context and creates a bean instance.
     * If the bean definition is not found, it returns null.
     * The class is thread-safe and can be used in a multi-threaded environment.
     * @param beanName The name of the bean to return.
     * @param clazz The class of the bean to return.
     * @return The bean instance, or null if the bean definition is not found.
     * @throws Exception If there is an error while returning the bean instance.
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

    @Override
    public Object refresh() throws Exception {
        return null;
    }

    @Override
    public void loadContext() {
        refreshContext();
    }

    @Override
    public void registerBeanPostProcessor(BeanPostProcessor processor) {
        super.registerBeanPostProcessor(processor);
    }
}
