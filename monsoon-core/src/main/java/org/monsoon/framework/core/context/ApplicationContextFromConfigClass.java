package org.monsoon.framework.core.context;

import org.monsoon.framework.core.annotations.*;
import org.monsoon.framework.core.utils.ScanPackageUtil;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class ApplicationContextFromConfigClass implements ApplicationContext {
    private final Map<String, BeanDefinition> beanDefinitions = new HashMap<>();
    private Map<String, Object> singletonBeans = new HashMap<>();
    private List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    public ApplicationContextFromConfigClass(Class<?> mainClass) throws Exception {
        Package basePackage = mainClass.getPackage();
        String basePackageName = basePackage.getName();
        List<Class<?>> classes = ScanPackageUtil.scanAllPackageForClasses(basePackageName);
        classes.addAll(ScanPackageUtil.scanMonsoonPackageForClasses());
        if (isAnnotationPresent(mainClass, ComponentScan.class)){
            scanComponents(classes);
        }
        registerBeanPostProcessor(classes);
    }

    private void scanComponents(List<Class<?>> classes) {
        for (Class<?> clazz: classes){
            if (isAnnotationPresent(clazz, Component.class)){
                registerComponents(clazz);
            }
        }
    }

    private void registerComponents(Class<?> clazz) {
        Component component = findAnnotation(clazz, Component.class);
        String beanName = component.name();

        if (beanName.equals("")) beanName = Introspector.decapitalize(clazz.getSimpleName());

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

    private <A extends Annotation> A findAnnotation(Class<?> source, Class<A> target) {
        if (source.isAnnotationPresent(target)) {
            return source.getAnnotation(target);
        }

        for (Annotation ann : source.getAnnotations()){
            A meta = findAnnotation(ann.annotationType(), target);
            if (meta != null) return meta;
        }
        return null;
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

    private Object createInstance(Class<?> clazz) throws Exception {
        Object instance = null;
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        if (constructors.length == 0){
            instance = applyProcessor(clazz);
            if (instance == null) throw new IllegalStateException("No constructors found for class: " + clazz.getName());
        }else{
            Constructor<?> targetConstructor = Arrays.stream(constructors)
                    .filter(c -> c.isAnnotationPresent(Autowired.class))
                    .findFirst()
                    .orElseGet(() -> constructors[0]);

            Class<?>[] paramTypes = targetConstructor.getParameterTypes();

            if (paramTypes.length > 0){
                Object[] params = new Object[paramTypes.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    params[i] = getBean(paramTypes[i]); // recursion
                }

                targetConstructor.setAccessible(true);
                instance = targetConstructor.newInstance(params);
            } else {
                targetConstructor.setAccessible(true);
                instance = targetConstructor.newInstance();
            }
        }

        injectDependencies(instance);
        return instance;
    }

    private void injectDependencies(Object instance) throws Exception {
        if (instance == null) return;
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

    private void registerBeanPostProcessor(List<Class<?>> classes) throws Exception {
        for (Class<?> clazz: classes){
            for (Class<?> inter : clazz.getInterfaces()){
                if (inter.equals(BeanPostProcessor.class)){
                    beanPostProcessors.add((BeanPostProcessor) createInstance(clazz));
                }
            }
        }
    }

    private Object applyProcessor(Class<?> clazz) {
        Object instance;
        for (BeanPostProcessor processor: beanPostProcessors){
            instance = processor.postProcess(clazz);
            if ( instance != null ) return instance;
        }
        return null;
    }
}
