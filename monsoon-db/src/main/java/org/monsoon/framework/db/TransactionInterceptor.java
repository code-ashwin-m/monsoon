package org.monsoon.framework.db;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.monsoon.framework.db.annotations.Transactional;

import java.lang.reflect.Method;
import java.sql.Connection;

public class TransactionInterceptor implements MethodInterceptor {
    private Object bean;
    private DataSourceProperty dataSource;

    public TransactionInterceptor(DataSourceProperty dataSource, Object bean) {
        this.bean = bean;
        this.dataSource = dataSource;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Method targetMethod =
                bean.getClass().getMethod(method.getName(), method.getParameterTypes());

        if (!targetMethod.isAnnotationPresent(Transactional.class)) {
            return method.invoke(bean, args);
        }

        try {
            Connection conn = ConnectionFactory.getConnection(dataSource);
            TransactionManager.begin(conn);
            Object result = method.invoke(bean, args);
            TransactionManager.commit();
            return result;
        } catch (Throwable e) {
            TransactionManager.rollback();
            throw e;
        }
    }
}
