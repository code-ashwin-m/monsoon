package org.monsoon.framework.core;

public class BeanDefinition {
    private final Boolean singleton;
    private Class<?> beanClass;
    private String beanName;

    public BeanDefinition(Class<?> beanClass, String beanName, Boolean singleton) {
        this.beanClass = beanClass;
        this.beanName = beanName;
        this.singleton = singleton;
    }

    public Boolean isSingleton() {
        return singleton;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public String getBeanName() {
        return beanName;
    }

    @Override
    public String toString() {
        return "BeanDefinition{" +
                "singleton=" + singleton +
                ", beanClass=" + beanClass +
                ", beanName='" + beanName + '\'' +
                '}';
    }
}
