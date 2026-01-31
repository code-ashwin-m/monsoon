package org.monsoon.framework.db.datapersister;

import org.monsoon.framework.db.interfaces.DataPersister;

public class DefaultDataPersister implements DataPersister<Object> {
    @Override
    public String javaToSql(Object value) {
        return value == null ? null : value.toString();
    }

    @Override
    public Object sqlToJava(Object value) {
        return value;
    }
}
