package org.monsoon.framework.core;

public class ClassUtils {
    public static boolean isPresent(String className) {
        try{
            Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            return true;
        } catch (Throwable ex){
            return false;
        }
    }
}
