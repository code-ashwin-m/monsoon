package org.monsoon.framework.core.utils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtils {

    public static List<Class<?>> scanAllPackageForClasses(String basePackage) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = basePackage.replace(".", "/");
        Enumeration<URL> resources = Thread.currentThread()
                .getContextClassLoader()
                .getResources(path);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();
            if (protocol.equals("file")){
                classes.addAll(findClassesInDirectory(basePackage, new File(resource.getFile())));
            } else if (protocol.equals("jar")) {
                classes.addAll(findClassesInJar(resource, path));
            }
        }

        return classes;
    }

    public static List<Class<?>> scanPackageForClasses(String basePackage) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = basePackage.replace(".", "/");
        URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
        String protocol = resource.getProtocol();
        if (protocol.equals("file")){
            classes.addAll(findClassesInDirectory(basePackage, new File(resource.getFile())));
        } else if (protocol.equals("jar")) {
            classes.addAll(findClassesInJar(resource, path));
        }
        return classes;
    }

    private static List<Class<?>> findClassesInDirectory(String basePackage, File directory) {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) return classes;

        File[] files = directory.listFiles();
        if (files == null) return classes;

        for (File file : files){
            if (file.isDirectory()) {
                classes.addAll(findClassesInDirectory(basePackage + "." + file.getName(), file));
            } else if (file.getName().endsWith(".class")) {
                String className = basePackage + "." + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
                    classes.add(clazz);
                } catch (Throwable ignored){}
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

    public static List<Class<?>> scanMonsoonPackageForClasses() throws Exception {
        return scanAllPackageForClasses("org.monsoon.framework");
    }

    public static boolean isPresent(String className) {
        try{
            Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            return true;
        } catch (Throwable ex){
            return false;
        }
    }
}
