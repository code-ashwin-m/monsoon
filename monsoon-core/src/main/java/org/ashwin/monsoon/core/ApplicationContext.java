package org.ashwin.monsoon.core;

import org.ashwin.monsoon.core.annotations.Component;
import org.ashwin.monsoon.core.annotations.ComponentScan;
import org.ashwin.monsoon.core.annotations.Inject;
import org.ashwin.monsoon.core.annotations.Singleton;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationContext {
    private final Map<Class<?>, Object> singletonBeanRegistry = new HashMap<>();

    public ApplicationContext(Class<?> mainClass) throws Exception {
        if (!mainClass.isAnnotationPresent(ComponentScan.class)){
            throw new RuntimeException("Main class must be annotated with @ComponentScan");
        }

        String basePackage = mainClass.getAnnotation(ComponentScan.class).value();
        List<Class<?>> classes = scanPackage(basePackage);

        for (Class<?> clazz: classes){
            registerSingletonBean(clazz);
        }
    }

    private List<Class<?>> scanPackage(String basePackage) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        String path = basePackage.replace(".", "/");
        URL root = Thread.currentThread().getContextClassLoader().getResource(path);

        if (root == null) {
            throw new RuntimeException("Package not found: " + basePackage);
        }

        File[] files = new File(root.getFile()).listFiles();
        if (files == null) {
            return classes;
        }

        for (File file : files) {
            if (file.isDirectory()){
                classes.addAll(scanPackage(basePackage + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = basePackage + "." + file.getName().substring(0, file.getName().length() - 6);
                classes.add(Class.forName(className));
            }
        }
        return classes;
    }

    private void registerSingletonBean(Class<?> clazz) throws Exception {
        if (isComponent(clazz) && clazz.isAnnotationPresent(Singleton.class)){
            Object instance;
            instance = clazz.getDeclaredConstructor().newInstance();
            injectDependencies(instance);
            singletonBeanRegistry.put(clazz, instance);
        }
    }



    public <T> T getBean(Class<T> clazz) throws Exception {
        if (!isComponent(clazz)){
            throw new RuntimeException("Class is not a Component: " + clazz.getName());
        }

        if (clazz.isAnnotationPresent(Singleton.class)){
            if (!singletonBeanRegistry.containsKey(clazz)) {
                throw new RuntimeException("Singleton not found: " + clazz.getName());
            }
            return clazz.cast(singletonBeanRegistry.get(clazz));
        }else{
            Object instance;
            instance = clazz.getDeclaredConstructor().newInstance();
            injectDependencies(instance);
            return clazz.cast(instance);
        }
    }

    private void injectDependencies(Object instance) throws Exception {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)){
                Object dependency = getBean(field.getType());
                if ( dependency == null) {
                    throw new RuntimeException("Dependency not found: " + field.getType().getName());
                }
                field.setAccessible(true);
                field.set(instance, dependency);
            }
        }
    }

    private boolean isComponent(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Component.class)) return true;

        for (Annotation ann : clazz.getAnnotations()){
            if ( ann.annotationType().isAnnotationPresent(Component.class)){
                return true;
            }
        }
        return false;
    }
}
