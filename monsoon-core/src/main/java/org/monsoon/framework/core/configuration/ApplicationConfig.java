package org.monsoon.framework.core.configuration;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ApplicationConfig {
    private static final Map<String, String> properties = new HashMap<>();

    static {
        try {
            InputStream input = ApplicationConfig.class.getClassLoader().getResourceAsStream("application.properties");
            if (input != null ) {
                loadProperties(input);
            } else {
                input = ApplicationConfig.class.getClassLoader().getResourceAsStream("application.yml");
                if (input != null ) {
                    loadYML(input);

                    Map<String, String> env = System.getenv();
                    for (String key : env.keySet()) {
                        properties.put(normalizeEnvKey(key), env.get(key));
                    }

                    String profile = get("app.profiles.active", "dev");
                    System.out.println("Profile: " + profile);
                    String profileFile = "application-" + profile + ".yml";
                    input = ApplicationConfig.class.getClassLoader().getResourceAsStream(profileFile);
                    if (input != null){
                        loadYML(input);
                    }


                } else {
                    System.out.println("application.properties or application.yml not found");
                }
            }
        } catch ( Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String normalizeEnvKey(String key) {
        return key.toLowerCase().replace("_", ".");
    }

    private static void loadProperties(InputStream input) throws Exception {
        Properties p = new Properties();
        p.load(input);
        for (String key : p.stringPropertyNames()) {
            properties.put(key, p.getProperty(key));
        }
    }

    private static void loadYML(InputStream input) {
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(input);
        if (map == null) return;
        flatten("", map);
    }

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
            } else {
                properties.put(key, val.toString());
            }
        }
    }

    public static String get(String key) {
        return properties.get(key);
    }

    public static String get(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    public static boolean hasPrefix(String prefix) {
        for ( String key : properties.keySet()){
            if (key.startsWith(prefix + ".")){
                return true;
            }
        }
        return false;
    }
}
