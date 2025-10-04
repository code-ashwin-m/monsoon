package org.ashwin.monsoon.db;

import org.ashwin.monsoon.db.annotations.Column;
import org.ashwin.monsoon.db.annotations.GeneratedId;
import org.ashwin.monsoon.db.annotations.Id;
import org.ashwin.monsoon.db.enums.GenerationType;
import org.ashwin.monsoon.db.interfaces.BaseDatabaseConfig;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RepositoryProxy implements InvocationHandler {
    private final Class<?> repositoryInterface;
    private final EntityMeta meta;
    private final BaseDatabaseConfig dbConfig;

    public RepositoryProxy(Class<?> repositoryInterface, EntityMeta meta, BaseDatabaseConfig dbConfig) {
        this.repositoryInterface = repositoryInterface;
        this.meta = meta;
        this.dbConfig = dbConfig;
    }

    public static <T> T create(Class<T> clazz, EntityMeta meta, BaseDatabaseConfig dbConfig) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[] { clazz },
                new RepositoryProxy(clazz, meta, dbConfig)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Connection conn = ConnectionFactory.getConnection(dbConfig);
        String methodName = method.getName();
        if (methodName.equals("create")){
            return create(args[0], conn);
        } else if (methodName.equals("createTableIfNotExists")) {
            return createTableIfNotExists(conn);
        }
        return "hello";
    }

    private String toSqlType(Class<?> type, String dbType, int width) {
        if (type == int.class || type == Integer.class) {
            if (dbType == "sqlite") return "INTEGER";
            return "INT";
        }
        if (type == long.class || type == Long.class) return "BIGINT";
        if (type == String.class) return "VARCHAR(" + width + ")";
        if (type == boolean.class || type == Boolean.class) {
            if (dbType.equals("sqlite")) return "BOOLEAN"; // SQLite treats as INTEGER 0/1
            return "BOOLEAN";
        }
        if (type == double.class || type == Double.class) return "DOUBLE";
        return "TEXT";
    }

    private String detectDbType(String url) {
        if (url.startsWith("jdbc:sqlite")) return "sqlite";
        if (url.startsWith("jdbc:mysql")) return "mysql";
        if (url.startsWith("jdbc:postgresql")) return "postgres";
        if (url.startsWith("jdbc:h2")) return "h2";
        return "generic";
    }

    private Object createTableIfNotExists(Connection conn) throws SQLException {
        String dbType = detectDbType("jdbc:sqlite");

        StringBuffer sql = new StringBuffer("CREATE TABLE IF NOT EXISTS " + meta.getTableName() + " (");
        List<String> uniqueDefs = new ArrayList<>();
        List<String> uniqueComboDefs = new ArrayList<>();

        List<Field> columns = meta.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            Field field = columns.get(i);
            Column column = field.getAnnotation(Column.class);

            sql.append(column.name()).append(" ").append(toSqlType(columns.get(i).getType(), dbType, column.width()));

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
        System.out.println(sql.toString());

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql.toString());
        }

        return true;
    }

    private Object create(Object arg, Connection conn) {
        return "65465-6545-45465-4654";
    }
}
