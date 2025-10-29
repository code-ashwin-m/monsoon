package org.monsoon.framework.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.type.MirroredTypeException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
}
