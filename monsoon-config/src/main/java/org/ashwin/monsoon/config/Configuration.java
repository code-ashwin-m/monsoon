package org.ashwin.monsoon.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Configuration {
    private static final Map<String, String> properties = new HashMap<>();

    static {
        try {
            try (InputStream input1 = Configuration.class.getClassLoader().getResourceAsStream("application.properties")){
                if (input1 != null) {
                    loadProperties(input1);
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadProperties(InputStream input) throws IOException {
        Properties p = new Properties();
        p.load(input);
        for (String key : p.stringPropertyNames()) {
            properties.put(key, p.getProperty(key));
        }
    }

    private static void loadYML(InputStream input) {
    }


    public static String get(String key) {
        return properties.get(key);
    }

}
