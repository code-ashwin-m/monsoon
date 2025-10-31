package org.monsoon.framework.core.properties;

import org.monsoon.framework.core.annotations.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PropertyBinder {
    private static final Logger logger = LoggerFactory.getLogger(PropertyBinder.class);

    public static <T> T bind(Class<T> target){
        if (!target.isAnnotationPresent(Property.class)){
            return null;
        }

        Property property = target.getAnnotation(Property.class);
        String prefix = (!property.value().equals("")) ? property.value() : property.prefix();
        if (prefix.trim().equals("")){
            return null;
        }

        try {
            T instance = target.getDeclaredConstructor().newInstance();
            bindFields(instance, prefix);
            return instance;
        }catch (Exception ex){

        }
        return null;
    }

    private static <T> void bindFields(T instance, String prefix) {
        for (Field field: instance.getClass().getDeclaredFields()){
            field.setAccessible(true);
            String propName = getPropertyName(instance, prefix, field);
            String propValue = ApplicationProperties.get(propName);

            Object obj = null;
            if (List.class.isAssignableFrom(field.getType())) {
                Type genericType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                Class<?> elementType = (Class<?>) genericType;
                List<Object> list = new ArrayList<>();
                int index = 0;
                while (ApplicationProperties.hasPrefix(propName + "[" + index + "]")){
                    Object element = null;
                    try {
                        if (isPrimitiveOrWrapperOrString(elementType)) {
                            String val = ApplicationProperties.get(propName + "[" + index + "]");
                            element = convertToType(val, elementType);
                        } else {
                            element = elementType.getDeclaredConstructor().newInstance();
                            bindFields(element, propName + "[" + index + "]");
                        }
                    } catch (Exception e) {
                    }
                    list.add(element);
                    index++;
                }
                obj = list;
            } else{
                if (propName != null) {
                    obj = convertToType(propValue, field.getType());
                } else if (ApplicationProperties.hasPrefix(propName)) {
                    try {
                        obj = field.getType().getDeclaredConstructor().newInstance();
                        bindFields(obj, propName);
                    } catch (Exception e) {
                    }
                }
            }

            try {
                field.set(instance, obj);
            } catch (IllegalAccessException e) {

            }
        }
    }

    private static <T> String getPropertyName(T instance, String prefix, Field field) {
        String prop;
        if( prefix.isEmpty() ){
            prop = field.getName();
        } else if ( instance.getClass().isAnnotationPresent(Property.class) ){
            if (prefix.equals(field.getName())) {
                prop = prefix;
            } else {
                prop = prefix + "." + field.getName();
            }
        } else {
            prop = prefix + "." + field.getName();
        }
        return prop;
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
        logger.error("Unsupported type: {}", targetType.getName());
        return null;
    }
}
