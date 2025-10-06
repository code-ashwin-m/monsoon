package org.monsoon.framework.core.context;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ApplicationContext implements Context{
    private List<Class<?>> classes = new ArrayList<>();
    public ApplicationContext(Class<?> mainClass) throws Exception {
        Package basePackage = mainClass.getPackage();
        String basePackageName = basePackage.getName();
        this.classes = scanPackageForClasses(basePackageName);
    }

    private static List<Class<?>> scanPackageForClasses(String basePackage) throws Exception {
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
                    classes.add(Class.forName(className));
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

    public List<Class<?>> getClasses() {
        return classes;
    }
}
