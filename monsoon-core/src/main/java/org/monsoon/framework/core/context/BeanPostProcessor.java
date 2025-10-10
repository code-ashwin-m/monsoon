package org.monsoon.framework.core.context;

public interface BeanPostProcessor {
    Object postProcess(Class<?> clazz);
}
