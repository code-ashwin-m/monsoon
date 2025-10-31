package org.monsoon.framework.core.properties;

import org.monsoon.framework.core.Monsoon;
import org.monsoon.framework.core.autoconfigure.YamlAutoConfiguration;
import org.monsoon.framework.core.interfaces.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This class is used to load application properties from different sources such as application.properties or application.yml files, and also from environment variables.
 * It also supports profile-based configuration.
 * The properties are stored in a Map, which can be accessed using the get method.
 * The class is thread-safe and can be used in a multi-threaded environment.
 */
public class ApplicationProperties {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationProperties.class);
    private static final Map<String, String> properties = new HashMap<>();
    private static YamlAutoConfiguration.YamlHelper yamlHelper = null;

    /**
     * This static block is used to load properties from application.properties or application.yml files,
     * and also from environment variables.
     * It also supports profile-based configuration.
     */
    static {
        yamlHelper = Monsoon.getContext().getBeanOrNull("yamlHelper", YamlAutoConfiguration.YamlHelper.class);
        ClassLoader classLoader = ApplicationProperties.class.getClassLoader();
        boolean loaded = false;

        try {

            try (InputStream input = classLoader.getResourceAsStream("application.properties")) {
                if (input != null) {
                    loadProperties(input);
                    logger.debug("Loaded properties from application.properties");
                    loaded = true;
                }
            }

            if (!loaded && yamlHelper != null) {
                try (InputStream input = classLoader.getResourceAsStream("application.yml")) {
                    if (input != null) {
                        loadYML(input);
                        logger.debug("Loaded properties from application.yml");
                        loaded = true;
                    }

                    System.getenv().forEach((key, value) ->
                            properties.put(normalizeEnvKey(key), value)
                    );

                    String profile = get("app.profiles.active", "dev");
                    String profileFile = "application-" + profile + ".yml";

                    try (InputStream profileInput = classLoader.getResourceAsStream(profileFile)) {
                        if (profileInput != null) {
                            loadYML(profileInput);
                            logger.debug("Loaded profile-specific properties from {}", profileFile);
                        } else {
                            logger.debug("Profile file {} not found — skipping", profileFile);
                        }
                    }
                }
            }

            if (!loaded) {
                logger.error("application.properties or application.yml not found in classpath");
            }
        } catch ( Exception ex) {
            logger.error("Failed to load application configuration", ex);
        }
    }

    /**
     * This method normalizes environment variable keys to be used as property keys.
     * It converts all characters to lowercase and replaces underscores with dots.
     * @param key The environment variable key to normalize.
     * @return The normalized property key.
     */
    private static String normalizeEnvKey(String key) {
        return key.toLowerCase().replace("_", ".");
    }

    /**
     * This method loads properties from a properties file.
     * @param input The input stream of the properties file.
     * @throws Exception If there is an error while loading the properties file.
     */
    private static void loadProperties(InputStream input) throws Exception {
        Properties p = new Properties();
        p.load(input);
        for (String key : p.stringPropertyNames()) {
            properties.put(key, p.getProperty(key));
        }
    }

    /**
     * This method loads properties from a YAML file.
     * @param input The input stream of the YAML file.
     */
    private static void loadYML(InputStream input) {
        Map<String, Object> map = yamlHelper.load(input);
        if (map == null) return;
        flatten("", map);
    }

    /**
     * This method recursively flattens a map into a single level map.
     * @param prefix The prefix to be added to the keys.
     * @param map The map to flatten.
     */
    private static void flatten(String prefix, Map<String, Object> map) {
        for (Map.Entry<String, Object> e : map.entrySet()) {
            String key = prefix.isEmpty() ? e.getKey() : prefix + "." + e.getKey();
            Object val = e.getValue();
            if (val instanceof Map) {
                flatten(key, (Map<String, Object>) val);
            } else if (val instanceof List) {
                for (int i = 0; i < ((List<?>) val).size(); i++) {
                    flatten(key + "[" + i + "]", (Map<String, Object>) ((List<?>) val).get(i));
                }
            } else if (val != null) {
                properties.put(key, val.toString());
            }
        }
    }

    /**
     * This method returns the value of the given property key.
     * If the property is not found, it returns null.
     * @param key The property key.
     * @return The value of the given property key, or null if the property is not found.
     */
    public static String get(String key) {
        return properties.get(key);
    }

    /**
     * This method returns the value of the given property key, or the given default value if the property is not found.
     * @param key The property key.
     * @param defaultValue The default value to return if the property is not found.
     * @return The value of the given property key, or the given default value if the property is not found.
     */
    public static String get(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    /**
     * This method checks if there is any property key that starts with the given prefix.
     * @param prefix The prefix to check.
     * @return True if there is any property key that starts with the given prefix, false otherwise.
     */
    public static boolean hasPrefix(String prefix) {
        for ( String key : properties.keySet()){
            if (key.startsWith(prefix + ".")){
                return true;
            }
        }
        return false;
    }
}
