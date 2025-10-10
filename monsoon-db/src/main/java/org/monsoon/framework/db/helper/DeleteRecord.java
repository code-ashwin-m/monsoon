package org.monsoon.framework.db.helper;

import org.monsoon.framework.db.EntityMeta;
import org.monsoon.framework.db.annotations.Column;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DeleteRecord {

    public static Boolean execute(Connection conn, String sql, Object[] args) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    stmt.setObject(i + 1, args[i]);
                }
            }
            stmt.executeUpdate();
        }
        return true;
    }

    public static Boolean deleteOne(Connection conn, EntityMeta meta, Object entity) throws Exception {
        if (entity == null) return false;

        SQLData sqlData = generateSQL(entity, meta);
        System.out.println(sqlData.getSql());

        try (PreparedStatement stmt = conn.prepareStatement(sqlData.getSql())) {
            for (int i = 0; i < sqlData.getValues().size(); i++) {
                stmt.setObject(i + 1, sqlData.getValues().get(i));
            }
            stmt.executeUpdate();
        }

        return true;
    }
    public static Boolean deleteMany(Connection conn, EntityMeta meta, List entities) throws Exception {
        if (entities == null || entities.isEmpty()) return false;

        SQLData sqlData = generateSQL(entities.get(0), meta);
        String sql = sqlData.getSql();
        System.out.println(sql);

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Prepare batch
            for (Object entity : entities) {
                SQLData data = generateSQL(entity, meta);

                // Bind values
                List<Object> values = data.getValues();
                for (int i = 0; i < values.size(); i++) {
                    stmt.setObject(i + 1, values.get(i));
                }
                stmt.addBatch();
            }

            // Execute batch
            stmt.executeBatch();
        }
        return true;
    }


    private static SQLData generateSQL(Object entity, EntityMeta meta) throws Exception {
        StringBuilder sql = new StringBuilder("DELETE FROM " + meta.getTableName());
        List<Object> values = new ArrayList<>();

        Field idField = meta.getIdField();
        idField.setAccessible(true);
        Object idValue = idField.get(entity);
        if (idValue == null) {
            throw new RuntimeException("Cannot delete entity without ID value");
        }
        sql.append(" WHERE ")
                .append(idField.getAnnotation(Column.class).name())
                .append(" = ?");
        values.add(idValue);

        return new SQLData(sql.toString(), values);
    }

}
