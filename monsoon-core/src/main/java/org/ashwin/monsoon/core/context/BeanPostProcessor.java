package org.ashwin.monsoon.core.context;

public interface BeanPostProcessor {
    Object postProcess(Class<?> clazz, Object instance);
}
