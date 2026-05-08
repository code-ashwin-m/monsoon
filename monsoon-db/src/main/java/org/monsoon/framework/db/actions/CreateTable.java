package org.monsoon.framework.db.actions;

import org.monsoon.framework.db.EntityMeta;
import org.monsoon.framework.db.annotations.Column;
import org.monsoon.framework.db.annotations.Entity;
import org.monsoon.framework.db.annotations.GeneratedId;
import org.monsoon.framework.db.annotations.Id;
import org.monsoon.framework.db.enums.GenerationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CreateTable {
    private static final Logger logger = LoggerFactory.getLogger(CreateTable.class);

    public static Boolean createTableIfNotExists(Connection conn, EntityMeta meta) throws SQLException {
        String dbType = Utils.detectDbType(conn);

        SQLData sqlData = generateSQL(meta, dbType);
        String sql = sqlData.getSql();

        logger.debug(sql);

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            return true;
        }
    }

    static SQLData generateSQL(EntityMeta meta, String dbType) {
        List<String> uniqueDefs = new ArrayList<>();
        List<String> uniqueComboDefs = new ArrayList<>();
        List<String> primaryKey = new ArrayList<>();
        List<String> foreignDefs = new ArrayList<>();

        StringBuffer sql = new StringBuffer("CREATE TABLE IF NOT EXISTS " + meta.getTableName() + " (");

        List<Field> columns = meta.getColumns();

        for (int i = 0; i < columns.size(); i++) {
            Field field = columns.get(i);
            Column column = field.getAnnotation(Column.class);
            String columnName = !column.name().isEmpty() ? column.name() : field.getName();
            sql.append(columnName)
                    .append(" ")
                    .append(toSqlType(columns.get(i).getType(), dbType))
                    .append(setNotNull(column))
                    .append(setDefaultValue(field, column, dbType));

            if (columns.get(i).isAnnotationPresent(Id.class)) {
                if (field.isAnnotationPresent(GeneratedId.class)) {
                    primaryKey.add(columnName);
                    GeneratedId gid = field.getAnnotation(GeneratedId.class);
                    if (gid.strategy() == GenerationType.AUTO) {
                        if (dbType.equals("sqlite"))
                            primaryKey.add("AUTOINCREMENT");
                    }
                }
            }

            if (column.unique()) {
                uniqueDefs.add("UNIQUE(" + columnName + ")");
            }

            if (column.uniqueCombo()) {
                uniqueComboDefs.add(columnName);
            }

            if (column.foreign() != Void.class) {
                if (column.foreign().isAnnotationPresent(Entity.class)) {
                    Entity entity = column.foreign().getAnnotation(Entity.class);
                    String ondelete = "";
                    if (column.cascadeDelete()) {
                        ondelete = " ON DELETE CASCADE";
                    }
                    foreignDefs.add(
                            "FOREIGN KEY(" + columnName + ") REFERENCES " + entity.tableName() + "(id)" + ondelete);
                }
            }

            if (i < columns.size() - 1) {
                sql.append(", ");
            }
        }

        if (!primaryKey.isEmpty()) {
            String key = String.join(" ", primaryKey);
            sql.append(", PRIMARY KEY (")
                    .append(key)
                    .append(")");
        }

        if (!uniqueComboDefs.isEmpty()) {
            String cols = String.join(", ", uniqueComboDefs);
            uniqueDefs.add("UNIQUE(" + cols + ")");
        }

        if (!uniqueDefs.isEmpty()) {
            sql.append(", ").append(String.join(", ", uniqueDefs));
        }

        if (!foreignDefs.isEmpty()) {
            sql.append(", ").append(String.join(", ", foreignDefs));
        }

        sql.append(")");

        return new SQLData(sql.toString(), null);
    }

    private static String setNotNull(Column column) {
        if (!column.notNull())
            return "";
        return " NOT NULL";
    }

    private static String setDefaultValue(Field field, Column column, String dbType) {
        String object = column.defaultValue();
        if (object == null || object.isEmpty())
            return "";

        if (field.getType() == int.class || field.getType() == Integer.class) {
            return " DEFAULT " + object;
        }
        if (field.getType() == long.class || field.getType() == Long.class) {
            return " DEFAULT " + object;
        }
        if (field.getType() == double.class || field.getType() == Double.class) {
            return " DEFAULT " + object;
        }

        if (field.getType() == boolean.class || field.getType() == Boolean.class) {
            if (dbType == "sqlite")
                return " DEFAULT " + (object.equalsIgnoreCase("true") ? 1 : 0);
            else
                return " DEFAULT " + (object.equalsIgnoreCase("true") ? "true" : "false");
        }

        if (field.getType() == String.class) {
            return " DEFAULT \"" + object + "\"";
        }

        return "";
    }

    private static String toSqlType(Class<?> type, String dbType) {
        if (type == int.class || type == Integer.class) {
            if (dbType == "sqlite")
                return "INTEGER";
            return "INT";
        }

        if (type == long.class || type == Long.class) {
            if (dbType == "sqlite")
                return "REAL";
            return "BIGINT";
        }

        if (type == String.class) {
            if (dbType == "sqlite")
                return "TEXT";
            return "VARCHAR(255)";
        }
        if (type == boolean.class || type == Boolean.class) {
            if (dbType.equals("sqlite"))
                return "INTEGER";
            return "BOOLEAN";
        }
        if (type == double.class || type == Double.class) {
            if (dbType == "sqlite")
                return "REAL";
            return "DOUBLE";
        }
        return "TEXT";
    }
}
