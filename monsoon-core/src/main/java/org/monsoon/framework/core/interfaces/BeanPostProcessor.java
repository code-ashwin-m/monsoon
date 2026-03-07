package org.monsoon.framework.core.interfaces;

public interface BeanPostProcessor {
    Object createInstance(Class<?> beanClass);

    Object postProcessBeforeInitialization(Object bean, Class<?> beanClass);

    Object postProcessAfterInitialization(Object bean, Class<?> beanClass);
}
