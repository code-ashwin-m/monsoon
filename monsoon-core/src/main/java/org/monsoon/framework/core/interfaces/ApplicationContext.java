package org.monsoon.framework.core.interfaces;

public interface ApplicationContext {
    Object getBean(String beanName) throws Exception;

    <T> T getBean(String beanName, Class<T> clazz) throws Exception;

    Object getBeanOrNull(String beanName);

    <T> T getBeanOrNull(String beanName, Class<T> clazz);

    Object refresh() throws Exception;
}
