package org.monsoon.framework.core.context;

import org.monsoon.framework.core.annotations.Autowired;
import org.monsoon.framework.core.annotations.Component;
import org.monsoon.framework.core.annotations.ComponentScan;
import org.monsoon.framework.core.annotations.Singleton;
import org.monsoon.framework.core.utils.ScanPackageUtil;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationContextFromConfigClass implements ApplicationContext {
    private final Map<String, BeanDefinition> beanDefinitions = new HashMap<>();
    private Map<String, Object> singletonBeans = new HashMap<>();

    public ApplicationContextFromConfigClass(Class<?> mainClass) throws Exception {
        Package basePackage = mainClass.getPackage();
        String basePackageName = basePackage.getName();
        List<Class<?>> classes = ScanPackageUtil.scanAllPackageForClasses(basePackageName);
        System.out.println(classes);
        if (isAnnotationPresent(mainClass, ComponentScan.class)){
            scanComponents(classes);
        }
    }

    private void scanComponents(List<Class<?>> classes) {
        for (Class<?> clazz: classes){
            if (isAnnotationPresent(clazz, Component.class)){
                registerComponents(clazz);
            }
        }
    }

    private void registerComponents(Class<?> clazz) {
        String beanName = Introspector.decapitalize(clazz.getSimpleName());
        Boolean isSingleton = isAnnotationPresent(clazz, Singleton.class);
        BeanDefinition def = new BeanDefinition(isSingleton, clazz, beanName);
        beanDefinitions.put(beanName, def);
    }

    private boolean isAnnotationPresent(Class<?> source, Class<?> target) {
        if (source.isAnnotationPresent((Class<? extends Annotation>) target)) return true;

        for (Annotation ann : source.getAnnotations()){
            if ( ann.annotationType().isAnnotationPresent((Class<? extends Annotation>) target)){
                return true;
            }
        }
        return false;
    }

    @Override
    public <T> T getBean(String beanName, Class<T> beanClass) throws Exception {
        return beanClass.cast(getBean(beanName));
    }

    @Override
    public <T> T getBean(Class<T> beanClass) throws Exception {
        String beanName = Introspector.decapitalize(beanClass.getSimpleName());
        return beanClass.cast(getBean(beanName));
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

    private Object createInstance(Class<?> clazz) throws Exception {
        Object instance = clazz.getDeclaredConstructor().newInstance();
        injectDependencies(instance);
        return instance;
    }

    private void injectDependencies(Object instance) throws Exception {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)){
                Object dependency = getBean(field.getType());
                if ( dependency == null) {
                    throw new RuntimeException("Dependency not found: " + field.getType().getName());
                }
                field.setAccessible(true);
                field.set(instance, dependency);
            }
        }
    }
}
