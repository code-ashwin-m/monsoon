package org.monsoon.framework.db.helper;

import org.monsoon.framework.db.EntityMeta;
import org.monsoon.framework.db.annotations.Column;
import org.monsoon.framework.db.annotations.GeneratedId;
import org.monsoon.framework.db.annotations.Id;
import org.monsoon.framework.db.enums.GenerationType;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CreateTable {
    public static Boolean createTableIfNotExists(Connection conn, EntityMeta meta) throws SQLException {
        String url = conn.getMetaData().getURL().toLowerCase();
        String dbType = detectDbType(url);

        SQLData sqlData = generateSQL(meta, dbType);
        String sql = sqlData.getSql();

        System.out.println(sql);

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }

        return true;
    }

    private static SQLData generateSQL(EntityMeta meta, String dbType) {
        List<String> uniqueDefs = new ArrayList<>();
        List<String> uniqueComboDefs = new ArrayList<>();

        StringBuffer sql = new StringBuffer("CREATE TABLE IF NOT EXISTS " + meta.getTableName() + " (");

        List<Field> columns = meta.getColumns();

        for (int i = 0; i < columns.size(); i++) {
            Field field = columns.get(i);
            Column column = field.getAnnotation(Column.class);
            String columnName = !column.name().isEmpty() ? column.name() : field.getName();
            sql.append(columnName).append(" ").append(toSqlType(columns.get(i).getType(), dbType));

            if (columns.get(i).isAnnotationPresent(Id.class)){
                sql.append(" PRIMARY KEY");
                if (field.isAnnotationPresent(GeneratedId.class)) {
                    GeneratedId gid = field.getAnnotation(GeneratedId.class);
                    if ( gid.strategy() == GenerationType.AUTO ) {
                        if (dbType.equals("sqlite")) sql.append(" AUTOINCREMENT");
                    }
                }
            }

            if (column.unique()){
                uniqueDefs.add("UNIQUE(" + column.name() + ")");
            }

            if (column.uniqueCombo()){
                uniqueComboDefs.add(column.name());
            }
            if (i < columns.size() - 1){
                sql.append(", ");
            }
        }

        if (!uniqueComboDefs.isEmpty()){
            String cols = String.join(", ", uniqueComboDefs);
            uniqueDefs.add("UNIQUE(" + cols + ")");
        }

        if (!uniqueDefs.isEmpty()){
            sql.append(", ").append(String.join(", ", uniqueDefs));
        }

        sql.append(")");

        return new SQLData(sql.toString(), null);
    }

    private static String detectDbType(String url) {
        if (url.startsWith("jdbc:sqlite")) return "sqlite";
        if (url.startsWith("jdbc:mysql")) return "mysql";
        if (url.startsWith("jdbc:postgresql")) return "postgres";
        if (url.startsWith("jdbc:h2")) return "h2";
        return "generic";
    }

    private static String toSqlType(Class<?> type, String dbType) {
        if (type == int.class || type == Integer.class) {
            if (dbType == "sqlite") return "INTEGER";

            return "INT";
        }
        if (type == long.class || type == Long.class) return "BIGINT";
        if (type == String.class) return "VARCHAR(255)";
        if (type == boolean.class || type == Boolean.class) {
            if (dbType.equals("sqlite")) return "BOOLEAN"; // SQLite treats as INTEGER 0/1
            return "BOOLEAN";
        }
        if (type == double.class || type == Double.class) return "DOUBLE";
        return "TEXT";
    }
}
