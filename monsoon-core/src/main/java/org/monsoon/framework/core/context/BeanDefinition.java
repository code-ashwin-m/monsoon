package org.monsoon.framework.core.context;

public class BeanDefinition {
    private boolean isSingleton = false;
    private Class<?> beanClass;
    private String beanName;

    public BeanDefinition(boolean isSingleton, Class<?> beanClass, String beanName) {
        this.isSingleton = isSingleton;
        this.beanClass = beanClass;
        this.beanName = beanName;
    }

    public boolean isSingleton() {
        return isSingleton;
    }

    public void setSingleton(boolean singleton) {
        isSingleton = singleton;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public String toString() {
        return "BeanDefinition{" +
                "isSingleton=" + isSingleton +
                ", beanClass=" + beanClass +
                ", beanName='" + beanName + '\'' +
                '}';
    }
}
