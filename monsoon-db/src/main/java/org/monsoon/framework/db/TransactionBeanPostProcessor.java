package org.monsoon.framework.db;

import net.sf.cglib.proxy.Enhancer;
import org.monsoon.framework.core.interfaces.BeanPostProcessor;
import org.monsoon.framework.db.annotations.Transactional;

import java.lang.reflect.Method;

public class TransactionBeanPostProcessor implements BeanPostProcessor {
    private DataSourceProperty dataSource;

    public TransactionBeanPostProcessor(DataSourceProperty dataSource){
        this.dataSource = dataSource;
    }

    @Override
    public Object createInstance(Class<?> beanClass) {
        return null;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, Class<?> beanClass) {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, Class<?> beanClass) {
        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Transactional.class)) {
                Object newBean = createProxy(bean);
                return newBean;
            }
        }
        return bean;
    }

    private Object createProxy(Object bean) {
        Class<?> beanClass = bean.getClass();

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(beanClass);
        enhancer.setCallback(new TransactionInterceptor(dataSource, bean));

        Object proxy = enhancer.create();
        return proxy;
    }

}
