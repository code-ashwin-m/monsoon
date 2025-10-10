package org.monsoon.framework.core.context;

public interface ApplicationContext {
    <T> T getBean(String beanName, Class<T> beanClass) throws Exception;
    <T> T getBean(Class<T> beanClass) throws Exception;
    Object getBean(String beanName) throws Exception;
}
