package org.monsoon.framework.db.interfaces;

public interface DataPersister<T> {
    Object javaToSql(Object value);
    T sqlToJava(Object value);
}
