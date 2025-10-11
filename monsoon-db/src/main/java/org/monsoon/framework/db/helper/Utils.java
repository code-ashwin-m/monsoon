package org.monsoon.framework.db.helper;

import org.monsoon.framework.db.annotations.Column;
import org.monsoon.framework.db.datapersister.DataPersisterRegistry;
import org.monsoon.framework.db.datapersister.DefaultDataPersister;
import org.monsoon.framework.db.interfaces.DataPersister;

import java.lang.reflect.Field;

public class Utils {
    public static DataPersister<?> resolveConvertor(Field field) throws Exception {
        Column column = field.getAnnotation(Column.class);
        Class<?> fieldType = field.getType();

        if (column != null && !column.convertor().equals(DefaultDataPersister.class)) {
            return column.convertor().getDeclaredConstructor().newInstance();
        }

        return DataPersisterRegistry.get(fieldType);
    }
}
