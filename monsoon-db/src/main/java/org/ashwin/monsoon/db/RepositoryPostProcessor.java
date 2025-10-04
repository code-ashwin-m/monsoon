package org.ashwin.monsoon.db;

import org.ashwin.monsoon.core.context.BeanPostProcessor;
import org.ashwin.monsoon.db.annotations.Column;
import org.ashwin.monsoon.db.annotations.Entity;
import org.ashwin.monsoon.db.annotations.Id;
import org.ashwin.monsoon.db.annotations.Repository;
import org.ashwin.monsoon.db.interfaces.BaseDatabaseConfig;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RepositoryPostProcessor implements BeanPostProcessor {
    private BaseDatabaseConfig dbConfig;

    public RepositoryPostProcessor(BaseDatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    @Override
    public Object postProcess(Class<?> clazz, Object instance) {
        if (clazz.isInterface() && clazz.isAnnotationPresent(Repository.class)){
            Repository repo = clazz.getAnnotation(Repository.class);
            Class<?> entityClass = repo.entity();
            EntityMeta meta = createMeta(entityClass);
            return RepositoryProxy.create(clazz, meta, dbConfig);
        }
        return instance;
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
