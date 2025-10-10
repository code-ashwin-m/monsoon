package org.monsoon.framework.db;

import java.lang.reflect.Field;
import java.util.List;

public class EntityMeta {
    private final Class<?> entityClass;
    private final String tableName;
    private final Field idField;
    private final List<Field> columns;

    public EntityMeta(Class<?> entityClass, String tableName, Field idField, List<Field> columns) {
        this.entityClass = entityClass;
        this.tableName = tableName;
        this.idField = idField;
        this.columns = columns;
    }

    public Class<?> getEntityClass() { return entityClass; }
    public String getTableName() { return tableName; }
    public Field getIdField() { return idField; }
    public List<Field> getColumns() { return columns; }
}
