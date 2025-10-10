package org.monsoon.framework.db;

import org.monsoon.framework.core.configuration.ConfigurationBinder;
import org.monsoon.framework.core.context.BeanPostProcessor;
import org.monsoon.framework.db.annotations.Column;
import org.monsoon.framework.db.annotations.Entity;
import org.monsoon.framework.db.annotations.Id;
import org.monsoon.framework.db.annotations.Repository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RepositoryPostProcessor implements BeanPostProcessor {
    private DatabaseConfiguration dbConfig;
    public RepositoryPostProcessor() {
        dbConfig = ConfigurationBinder.bind(DatabaseConfiguration.class);
    }

    @Override
    public Object postProcess(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Repository.class)) return null;

        Repository repo = clazz.getAnnotation(Repository.class);
        Class<?> entityClass = repo.entity();
        EntityMeta meta = createMeta(entityClass);
        Object object =  RepositoryProxy.create(clazz, meta, dbConfig);
        return object;
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
