package org.monsoon.framework.core;

import org.monsoon.framework.core.annotations.AutoConfigureAfter;
import org.monsoon.framework.core.annotations.AutoConfigureBefore;
import org.monsoon.framework.core.annotations.ConditionalOnMissingBean;
import org.monsoon.framework.core.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class AutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(AutoConfiguration.class);
    private static final String FACTORIES_RESOURCE = "META-INF/org.monsoon.framework.core.AutoConfiguration.imports";

    /**
     * Loads all auto configuration classes from the classpath.
     * The auto configuration classes are specified in the file
     * META-INF/monsoon.factories.
     * The file should contain a single property with the name
     * org.monsoon.framework.core.annotations.EnableAutoConfiguration
     * and the value should be a comma-separated list of class names.
     * The class names should be fully qualified.
     *
     * @return a list of auto configuration classes
     */
    public static List<String> loadClassNamesFromImports() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<String> result = new ArrayList<>();

        try {
            Enumeration<URL> resources = classLoader.getResources(FACTORIES_RESOURCE);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        result.add(line);
                    }
                }
            }
            return result;
        } catch (IOException e) {
            logger.error("Failed to load auto configuration", e);
        }
        return Collections.emptyList();
    }

    public static List<Class<?>> sort(List<Class<?>> classList) {
        Map<Class<?>, Set<Class<?>>> dependOn = new HashMap<>();

        for (Class<?> clazz : classList) {
            dependOn.putIfAbsent(clazz, new HashSet<>());
            AutoConfigureBefore before = clazz.getAnnotation(AutoConfigureBefore.class);
            if (before != null) {
                for (Class<?> target : before.value()) {
                    dependOn.putIfAbsent(clazz, new HashSet<>());
                    dependOn.get(clazz).add(target);
                }
                for (String name : before.name()) {
                    Class<?> target = ClassUtils.forName(name);
                    if (target != null) {
                        dependOn.putIfAbsent(clazz, new HashSet<>());
                        dependOn.get(clazz).add(target);
                    }
                }
            }

            AutoConfigureAfter after = clazz.getAnnotation(AutoConfigureAfter.class);
            if (after != null) {
                for (Class<?> target : after.value()) {
                    dependOn.putIfAbsent(target, new HashSet<>());
                    dependOn.get(target).add(clazz);
                }
                for (String name : after.name()) {
                    Class<?> target = ClassUtils.forName(name);
                    if (target != null) {
                        dependOn.putIfAbsent(target, new HashSet<>());
                        dependOn.get(target).add(clazz);
                    }
                }
            }

            ConditionalOnMissingBean missingBean = clazz.getAnnotation(ConditionalOnMissingBean.class);
            if (missingBean != null) {
                for (Class<?> target : missingBean.value()) {
                    dependOn.putIfAbsent(target, new HashSet<>());
                    dependOn.get(target).add(clazz);
                }
                for (String name : missingBean.name()) {
                    Class<?> target = ClassUtils.forName(name);
                    if (target != null) {
                        dependOn.putIfAbsent(target, new HashSet<>());
                        dependOn.get(target).add(clazz);
                    }
                }
            }
        }

        List<Class<?>> sortedList = topologicalSort(dependOn);
        sortedList.sort((a, b) -> {
            ConditionalOnMissingBean aAnno = a.getAnnotation(ConditionalOnMissingBean.class);
            ConditionalOnMissingBean bAnno = b.getAnnotation(ConditionalOnMissingBean.class);

                if (aAnno != null && bAnno != null){
                    if (Arrays.asList(aAnno.value()).contains(b)) return 1;
                    if (Arrays.asList(bAnno.value()).contains(a)) return -1;
                }

            if (aAnno != null && bAnno == null) return 1;
            if (aAnno == null && bAnno != null) return -1;

            return 0;
        });

        return sortedList;
    }

    private static List<Class<?>> topologicalSort(Map<Class<?>, Set<Class<?>>> graph) {
        Map<Class<?>, Integer> inDegree = new HashMap<>();
        for (Class<?> node : graph.keySet()) {
            inDegree.putIfAbsent(node, 0);
            for (Class<?> dep : graph.get(node)) {
                inDegree.put(dep, inDegree.getOrDefault(dep, 0) + 1);
            }
        }

        Queue<Class<?>> queue = new LinkedList<>();
        for (Map.Entry<Class<?>, Integer> e : inDegree.entrySet()) {
            if (e.getValue() == 0) queue.add(e.getKey());
        }

        List<Class<?>> sortedList = new ArrayList<>();
        while (!queue.isEmpty()) {
            Class<?> node = queue.poll();
            sortedList.add(node);
            for (Class<?> dep : graph.getOrDefault(node, Collections.emptySet())) {
                inDegree.put(dep, inDegree.get(dep) - 1);
                if (inDegree.get(dep) == 0) queue.add(dep);
            }
        }

        if (sortedList.size() < graph.size()) {
            sortedList.addAll(graph.keySet().stream()
                    .filter(c -> !sortedList.contains(c))
                    .collect(Collectors.toList())
            );
        }

        return sortedList;
    }
}
