package org.ashwin.monsoon.core;

import org.ashwin.monsoon.core.annotations.ComponentScan;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ApplicationContext {


    public ApplicationContext(Class<?> mainClass) throws ClassNotFoundException {
        if (!mainClass.isAnnotationPresent(ComponentScan.class)){
            throw new RuntimeException("Main class must be annotated with @ComponentScan");
        }

        String basePackage = mainClass.getAnnotation(ComponentScan.class).value();
        List<Class<?>> classes = scanPackage(basePackage);
        System.out.println(classes);
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
            if (file.getName().endsWith(".class")) {
                String className = basePackage + "." + file.getName().substring(0, file.getName().length() - 6);
                classes.add(Class.forName(className));
            }
        }
        return classes;
    }
}
