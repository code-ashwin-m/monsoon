package org.monsoon.framework.core.configuration;

import org.monsoon.framework.core.annotations.ConfigurationProperties;

import java.lang.reflect.Field;

public class ConfigurationBinder {
    public static <T> T bind(Class<T> clazz){
        if (!clazz.isAnnotationPresent(ConfigurationProperties.class)){
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with @ConfigurationProperties");
        }

        ConfigurationProperties cp = clazz.getAnnotation(ConfigurationProperties.class);
        String prefix = cp.prefix().isEmpty() ? "" : cp.prefix() + ".";

        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            bindFields(instance, prefix);
            return instance;
        } catch (Exception ex){
            throw new RuntimeException("Failed to bind configuration properties to class " + clazz.getName(), ex);
        }
    }

    private static <T> void bindFields(T config, String prefix) throws Exception {
        for (Field field : config.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            String propertyName = prefix + field.getName();
            String propertyValue = ApplicationConfig.get(propertyName);
            if (propertyValue != null) {
                Object value = convertToType(propertyValue, field.getType());
                field.set(config, value);
            } else {
                boolean hasNested = ApplicationConfig.hasPrefix(propertyName);
                if (hasNested){
                    Object nested = field.getType().getDeclaredConstructor().newInstance();
                    bindFields(nested, propertyName + ".");
                    field.set(config, nested);
                }
            }
        }
    }

    private static Object convertToType(String value, Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value);
        } else if (targetType.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) targetType, value);
        }
        throw new UnsupportedOperationException("Unsupported type: " + targetType.getName());
    }
}
