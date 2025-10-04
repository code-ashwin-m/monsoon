package org.ashwin.monsoon.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Configuration {
//    private static Properties properties = new Properties();
    private static Map<String, String> properties = new HashMap<>();
    static {
        try {
            try (InputStream input1 = Configuration.class.getClassLoader().getResourceAsStream("application.properties")){
                if (input1 != null) {
                    mergeProps(input1);
                    String activeProfile = get("app.profiles.active");
                    String profileFile = "application-" + activeProfile + ".properties";
                    try (InputStream input2 = Configuration.class.getClassLoader().getResourceAsStream(profileFile)){
                        if (input2 != null) {
                            mergeProps(input2);
                        }
                    }

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
                properties.put(key, systemProperties.getProperty(key));
            }

            Map<String, String> env = System.getenv();
            for (Map.Entry<String, String> e : env.entrySet()) {
                String key = normalizeEnvKey(e.getKey());
                properties.put(key, e.getValue());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void mergeProps(InputStream input) throws Exception {
        Properties props = new Properties();
        props.load(input);
        for (String key : props.stringPropertyNames()) {
            properties.put(key, props.getProperty(key));
        }
    }

    private static void loadYML(InputStream input) {
    }


    public static String get(String key) {
        return properties.get(key);
    }

    public static boolean hasPrefix(String prefix) {
        return properties.keySet().stream().anyMatch(key -> key.toString().startsWith(prefix));
    }

    private static String normalizeEnvKey(String key) {
        return key.toLowerCase().replace("_", ".");
    }
}
