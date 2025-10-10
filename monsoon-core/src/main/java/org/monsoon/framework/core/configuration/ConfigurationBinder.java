package org.monsoon.framework.core.configuration;

import org.monsoon.framework.core.annotations.ConfigurationProperties;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationBinder {
    public static <T> T bind(Class<T> clazz){
        if (!clazz.isAnnotationPresent(ConfigurationProperties.class)){
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with @ConfigurationProperties");
        }

        ConfigurationProperties cp = clazz.getAnnotation(ConfigurationProperties.class);
        String prefix = cp.prefix();

        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            bindFields(instance, prefix);
            return instance;
        } catch (Exception ex){
            throw new RuntimeException("Failed to bind configuration properties to class " + clazz.getName(), ex);
        }
    }

    private static <T> void bindFields(T instance, String prefix) throws Exception {
        for (Field field : instance.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            String propertyName = getPropertyName(instance, prefix, field);
            String propertyValue = ApplicationConfig.get(propertyName);

            if (List.class.isAssignableFrom(field.getType())){
                Type genericType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                Class<?> elementType = (Class<?>) genericType;
                List<Object> list = new ArrayList<>();
                int index = 0;
                while (ApplicationConfig.hasPrefix(propertyName + "[" + index + "]")){
                    Object element;
                    if (isPrimitiveOrWrapperOrString(elementType)) {
                        String val = ApplicationConfig.get(propertyName + "[" + index + "]");
                        element = convertToType(val, elementType);
                    } else {
                        element = elementType.getDeclaredConstructor().newInstance();
                        bindFields(element, propertyName + "[" + index + "]");
                    }
                    list.add(element);
                    index++;
                }


                field.set(instance, list);
            }else{
                if (propertyValue != null) {
                    Object value = convertToType(propertyValue, field.getType());
                    field.set(instance, value);
                } else if (ApplicationConfig.hasPrefix(propertyName)) {
                    Object nested = field.getType().getDeclaredConstructor().newInstance();
                    bindFields(nested, propertyName);
                    field.set(instance, nested);
                }
            }
        }
    }

    private static <T> String getPropertyName(T config, String prefix, Field field) {
        String propertyName;
        if (prefix.isEmpty()){
            propertyName = field.getName();
        } else if (config.getClass().isAnnotationPresent(ConfigurationProperties.class)) {
            if (prefix.equals(field.getName())){
                propertyName = prefix;
            } else {
                propertyName = prefix + "." + field.getName();
            }
        } else {
            propertyName = prefix + "." + field.getName();
        }
        return propertyName;
    }

    private static boolean isPrimitiveOrWrapperOrString(Class<?> type) {
        return type.isPrimitive()
                || type == String.class
                || type == Integer.class
                || type == Long.class
                || type == Double.class
                || type == Float.class
                || type == Boolean.class
                || type == Short.class
                || type == Byte.class
                || type == Character.class;
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
