package org.monsoon.framework.db;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;
import org.monsoon.framework.core.interfaces.BeanPostProcessor;
import org.monsoon.framework.db.annotations.Column;
import org.monsoon.framework.db.annotations.Entity;
import org.monsoon.framework.db.annotations.Id;
import org.monsoon.framework.db.annotations.Repository;

import java.lang.reflect.Field;

public class RepositoryPostProcessor implements BeanPostProcessor {
    private DataSourceProperty dataSource;
    public RepositoryPostProcessor(DataSourceProperty dataSource){
        this.dataSource = dataSource;

    }
    @Override
    public Object createInstance(Class<?> beanClass) {
        if (!beanClass.isAnnotationPresent(Repository.class)) return null;
        return createProxy(beanClass);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, Class<?> beanClass) {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, Class<?> beanClass) {
        return bean;
    }

    private Object createProxy(Class<?>  beanClass) {
        Repository repo = beanClass.getAnnotation(Repository.class);
        Class<?> entityClass = repo.entity();
        EntityMeta meta = createMeta(entityClass);

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(beanClass);
        enhancer.setCallback(new RepositoryInterceptor(beanClass, meta, dataSource));

        Object proxy = enhancer.create();
        return proxy;
    }

    private EntityMeta createMeta(Class<?> entityClass) {
        Entity entityAnn = entityClass.getAnnotation(Entity.class);
        String tableName = entityAnn.tableName();
        Field idField = null;
        List<Field> columns = new ArrayList<>();
        for (Field field : entityClass.getDeclaredFields()) {
            if ( field.isAnnotationPresent(Column.class)) {
                columns.add(field);
                if (field.isAnnotationPresent(Id.class)) {
                    idField = field;
                }
            }
        }
        return new EntityMeta(entityClass, tableName, idField, columns);
    }
}
