package org.monsoon.framework.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.type.MirroredTypeException;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtils {
    private static final Logger logger = LoggerFactory.getLogger(ClassUtils.class);
    public static boolean isPresent(String className) {
        try{
            Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    /**
     * This method checks if the given class is annotated with the given annotation.
     * It iterates over all the annotations of the class and checks if any of them is annotated with the given annotation.
     * @param source The class to check for annotations.
     * @param target The annotation to check for.
     * @return true if the class is annotated with the given annotation, false otherwise.
     */
    public static boolean isAnnotationPresent(Class<?> source, Class<?> target) {
        return isAnnotationPresentRecursive(source, (Class<? extends Annotation>) target, new HashSet<>());
    }

    public static boolean isAnnotationPresentRecursive(Class<?> source, Class<? extends Annotation> target, Set<Class<?>> visited) {
        if (source == null || visited.contains(source)) return false;
        visited.add(source);

        if (source.isAnnotationPresent(target)) {
            return true;
        }

        for (Annotation ann : source.getAnnotations()) {
            Class<? extends Annotation> annType = ann.annotationType();

            if (annType.getName().startsWith("java.lang.annotation")) continue;

            if (isAnnotationPresentRecursive(annType, target, visited)) {
                return true;
            }
        }

        return false;
    }


    /**
     * This method finds the given annotation on the given class.
     * It iterates over all the annotations of the class and checks if any of them is annotated with the given annotation.
     * @param source The class to find the annotation on.
     * @param target The annotation to find.
     * @return The annotation, or null if the annotation is not found.
     */
    public static <A extends Annotation> A findAnnotation(Class<?> source, Class<A> target) {
        return findAnnotationRecursive(source, target, new HashSet<>());
    }

    public static <A extends Annotation> A findAnnotationRecursive(Class<?> source, Class<A> target, Set<Class<?>> visited) {
        if (source == null || visited.contains(source)) return null;
        visited.add(source);

        if (source.isAnnotationPresent(target)) {
            return source.getAnnotation(target);
        }

        for (Annotation ann : source.getAnnotations()) {
            Class<? extends Annotation> annType = ann.annotationType();

            if (annType.getName().startsWith("java.lang.annotation")) continue;

            A meta = findAnnotationRecursive(annType, target, visited);
            if (meta != null) return meta;
        }

        return null;
    }

    public static <A extends Annotation> Optional<A> getAnnotationSafe(Class<?> clazz, Class<A> annotationType) {
        try {
            for (Annotation ann : clazz.getDeclaredAnnotations()) {
                if (annotationType.isInstance(ann)) {
                    return Optional.of((A) ann);
                }
            }
        } catch (TypeNotPresentException | ArrayStoreException | MirroredTypeException ignored) {
            // Skip problematic annotations
        } catch (Throwable t) {
            // Defensive fallback
            System.err.println("Skipping broken annotation on " + clazz.getName() + ": " + t);
        }
        return Optional.empty();
    }

    public static <T extends Annotation> T getAnnotationSafe2(Class<?> clazz, Class<T> annotationType) {
        try {
            for (Annotation ann : clazz.getDeclaredAnnotations()) {
                if (annotationType.isInstance(ann)) {
                    return (T) ann;
                }
            }
        } catch (TypeNotPresentException | ArrayStoreException | MirroredTypeException ignored) {
            System.err.println("Skipping broken annotation on " + clazz.getName() + ": " + ignored);
        } catch (Throwable t) {
            // Defensive fallback
            System.err.println("Skipping broken annotation on " + clazz.getName() + ": " + t);
        }
        return null;
    }

    public static Class<?> forName(String className) {
        try{
            return Class.forName(className, false, Thread.currentThread().getContextClassLoader());
        } catch (Throwable ex) {
            return null;
        }
    }

    /**
     * This method scans for classes in all packages.
     * It returns a list of classes found in the given package.
     * @param basePackageName The base package name to scan for classes.
     * @return A list of classes found in the given package.
     * @throws Exception If there is an error while scanning for classes.
     */
    public static List<Class<?>> scanForClasses(String basePackageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = basePackageName.replace(".", "/");

        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
        while (resources.hasMoreElements()){
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();
            logger.debug("Protocol is {}", protocol);
            if (protocol.equals("file")){
                logger.debug("Scan for classes in all package is started in local directory");
                classes.addAll(findClassesInDirectory(basePackageName, new File(resource.getFile())));
            } else if (protocol.equals("jar")) {
                logger.debug("Scan for classes in all package is started in jar");
                classes.addAll(findClassesInJar(resource, path));
            }
        }
        logger.debug("Scan for classes in all package is completed. Total classes {}", classes.size());
        return classes;
    }


    /**
     * This method finds all classes in the given directory and its subdirectories.
     * It iterates over all the files in the directory and checks if the file is a directory or a class file.
     * If the file is a directory, it recursively calls itself to find all classes in the subdirectory.
     * If the file is a class file, it tries to load the class and adds it to the list of classes.
     * @param basePackageName The base package name of the classes to find.
     * @param directory The directory to search for classes.
     * @return A list of classes found in the given directory and its subdirectories.
     */
    private static List<Class<?>> findClassesInDirectory(String basePackageName, File directory) {
        List<Class<?>> classes = new ArrayList<>();

        if (!directory.exists()) return classes;

        File[] files = directory.listFiles();
        if (files == null) return classes;

        for (File file : files){
            if (file.isDirectory()){
                classes.addAll(findClassesInDirectory(basePackageName + "." + file.getName(), file));
            }else {

                String className = basePackageName + "." + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
                    classes.add(clazz);
                } catch (Throwable ignored) {}
            }
        }
        return classes;
    }

    private static List<Class<?>> findClassesInJar(URL resource, String path) throws IOException {
        List<Class<?>> classes = new ArrayList<>();
        JarURLConnection connection = (JarURLConnection) resource.openConnection();
        JarFile jarFile = connection.getJarFile();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (entryName.startsWith(path) && entryName.endsWith(".class") && !entry.isDirectory()) {
                String className = entryName.replace("/", ".").substring(0, entryName.length() - 6);
                try {
                    classes.add(Class.forName(className));
                } catch (Throwable ignored){}
            }
        }
        return classes;
    }
}
