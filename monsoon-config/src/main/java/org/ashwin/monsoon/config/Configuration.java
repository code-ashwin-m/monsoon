package org.ashwin.monsoon.config;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class Configuration {
    private static Properties properties = new Properties();

    static {
        try {
            try (InputStream input1 = Configuration.class.getClassLoader().getResourceAsStream("application.properties")){
                if (input1 != null) {
                    properties.load(input1);
                }else{
                    try (InputStream input2 = Configuration.class.getClassLoader().getResourceAsStream("application.yml")){
                        if (input2 != null) {
                            loadYML(input2);
                        } else {
                            throw new RuntimeException("application.properties or application.yml not found");
                        }
                    }
                }
            }

            Properties systemProperties = System.getProperties();
            for (String key : systemProperties.stringPropertyNames()) {
                properties.setProperty(key, systemProperties.getProperty(key));
            }

            Map<String, String> env = System.getenv();
            for (Map.Entry<String, String> e : env.entrySet()) {
                String key = normalizeEnvKey(e.getKey());
                properties.setProperty(key, e.getValue());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadYML(InputStream input) {
    }


    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static boolean hasPrefix(String prefix) {
        return properties.keySet().stream().anyMatch(key -> key.toString().startsWith(prefix));
    }

    private static String normalizeEnvKey(String key) {
        return key.toLowerCase().replace("_", ".");
    }
}
