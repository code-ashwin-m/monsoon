package org.monsoon.framework.db.actions;

import org.monsoon.framework.db.annotations.Column;
import org.monsoon.framework.db.datapersister.DataPersisterRegistry;
import org.monsoon.framework.db.datapersister.DefaultDataPersister;
import org.monsoon.framework.db.interfaces.DataPersister;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

public class Utils {
    public static DataPersister<?> resolveConvertor(Field field) throws Exception {
        Column column = field.getAnnotation(Column.class);
        Class<?> fieldType = field.getType();

        if (column != null && !column.convertor().equals(DefaultDataPersister.class)) {
            return column.convertor().getDeclaredConstructor().newInstance();
        }

        return DataPersisterRegistry.get(fieldType);
    }

    public static String detectDbType(Connection conn) throws SQLException {
        String url = conn.getMetaData().getURL().toLowerCase();
        if (url.startsWith("jdbc:sqlite")) return "sqlite";
        if (url.startsWith("jdbc:mysql")) return "mysql";
        if (url.startsWith("jdbc:postgresql")) return "postgres";
        if (url.startsWith("jdbc:h2")) return "h2";
        return "generic";
    }
}
