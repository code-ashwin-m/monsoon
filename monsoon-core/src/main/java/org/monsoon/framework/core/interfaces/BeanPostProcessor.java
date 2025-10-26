package org.monsoon.framework.core.interfaces;

public interface BeanPostProcessor {
    Object postProcess(Class<?> clazz);
}
