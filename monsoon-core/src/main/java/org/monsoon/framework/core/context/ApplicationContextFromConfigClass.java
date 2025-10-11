package org.monsoon.framework.core.context;

import org.monsoon.framework.core.annotations.*;
import java.beans.Introspector;

public class ApplicationContextFromConfigClass extends ApplicationContextHelper implements ApplicationContext {
    public ApplicationContextFromConfigClass(Class<?> mainClass) throws Exception {
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
    public Object refresh() {
        return null;
    }
}
