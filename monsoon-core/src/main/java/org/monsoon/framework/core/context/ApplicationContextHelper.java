package org.monsoon.framework.core.context;

import org.monsoon.framework.core.annotations.*;
import org.monsoon.framework.core.utils.ClassUtils;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ApplicationContextHelper {
    protected Map<String, BeanDefinition> beanDefinitions = new HashMap<>();
    protected Map<String, Object> singletonBeans = new HashMap<>();
    protected List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    public ApplicationContextHelper(Class<?> mainClass) throws Exception {
        Package basePackage = mainClass.getPackage();
        String basePackageName = basePackage.getName();
        List<Class<?>> classes = ClassUtils.scanAllPackageForClasses(basePackageName);
        List<Class<?>> monsoonClasses = ClassUtils.scanMonsoonPackageForClasses();
        for (Class<?> clazz : monsoonClasses){
            if (!classes.contains(clazz)) classes.add(clazz);
        }
        if (isAnnotationPresent(mainClass, ComponentScan.class)){
            scanComponents(classes);
        }
        if (isAnnotationPresent(mainClass, AutoConfiguration.class)){
            scanAutoConfiguration(classes);
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

    private void scanAutoConfiguration(List<Class<?>> classes) throws Exception {
        System.out.println("Scanning for auto configuration");
        for (Class<?> clazz: classes){
            if (isAnnotationPresent(clazz, Configuration.class)){
                System.out.println("Registering configuration for " + clazz.getSimpleName());
                Object configInstance = clazz.getDeclaredConstructor().newInstance();
                registerConfiguration(configInstance, clazz);
            }
        }
    }

    private void registerConfiguration(Object configInstance, Class<?> clazz) throws Exception {
        for (Method method : clazz.getDeclaredMethods()){
            if (method.isAnnotationPresent(Bean.class)){
                method.setAccessible(true);
                String beanName = method.getName();
                BeanDefinition def = new BeanDefinition(true, method.getReturnType(), beanName);
                beanDefinitions.put(beanName, def);
                Object bean = method.invoke(configInstance);
                singletonBeans.put(beanName, bean);
            }
        }
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

    protected <A extends Annotation> A findAnnotation(Class<?> source, Class<A> target) {
        if (source.isAnnotationPresent(target)) {
            return source.getAnnotation(target);
        }

        for (Annotation ann : source.getAnnotations()){
            A meta = findAnnotation(ann.annotationType(), target);
            if (meta != null) return meta;
        }
        return null;
    }

    protected Object createInstance(Class<?> clazz) throws Exception {
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

    private Object getBean(Class<?> clazz) throws Exception {
        Component component = findAnnotation(clazz, Component.class);
        if (component == null) return null;
        String beanName = component.name();
        if (beanName.equals("")) beanName = Introspector.decapitalize(clazz.getSimpleName());
        return getBean(beanName);
    }

    private  Object getBean(String beanName) throws Exception {
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
}
