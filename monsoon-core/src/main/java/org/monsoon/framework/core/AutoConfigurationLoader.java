package org.monsoon.framework.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class AutoConfigurationLoader {
    private static final Logger logger = LoggerFactory.getLogger(AutoConfigurationLoader.class);
    private static final String FACTORIES_RESOURCE = "META-INF/monsoon.factories";
    private static final String KEY = "org.monsoon.framework.core.annotations.EnableAutoConfiguration";

    /**
     * Loads all auto configuration classes from the classpath.
     * The auto configuration classes are specified in the file
     * META-INF/monsoon.factories.
     * The file should contain a single property with the name
     * org.monsoon.framework.core.annotations.EnableAutoConfiguration
     * and the value should be a comma-separated list of class names.
     * The class names should be fully qualified.
     * @return a list of auto configuration classes
     */
    public static List<String> load() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<String> result = new ArrayList<>();
        try {
            Enumeration<URL> resources = classLoader.getResources(FACTORIES_RESOURCE);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream()))) {
                    String line;
                    StringBuilder currentLine = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        line = line.trim();

                        // Skip comments and empty lines
                        if (line.isEmpty() || line.startsWith("#")) continue;

                        // Handle line continuation with '\'
                        if (line.endsWith("\\")) {
                            currentLine.append(line, 0, line.length() - 1);
                            continue; // wait for next line
                        } else {
                            currentLine.append(line);
                        }

                        String fullLine = currentLine.toString();
                        currentLine.setLength(0); // reset buffer

                        // Only process our key
                        String prefix = "org.monsoon.framework.core.annotations.EnableAutoConfiguration=";
                        if (fullLine.startsWith(prefix)) {
                            String value = fullLine.substring(prefix.length()).trim();
                            // Split comma-separated values
                            String[] classes = value.split(",");
                            for (String cls : classes) {
                                cls = cls.trim();
                                if (!cls.isEmpty() && !result.contains(cls)) {
                                    result.add(cls);
                                }
                            }
                        }
                    }
                }
            }

            return result;
        } catch (IOException e) {
            logger.error("Failed to load auto configuration", e);
        }
        return Collections.emptyList();
    }
}
