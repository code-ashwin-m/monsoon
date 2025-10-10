package org.monsoon.framework.db.helper;

import org.monsoon.framework.db.EntityMeta;
import org.monsoon.framework.db.annotations.Column;
import org.monsoon.framework.db.annotations.GeneratedId;
import org.monsoon.framework.db.enums.GenerationType;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UpdateRecord {

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

    public static Boolean updateOne(Connection conn, EntityMeta meta, Object entity) throws Exception {
        if (entity == null) return false;
        boolean isGenerated = meta.getIdField().isAnnotationPresent(GeneratedId.class);
        GeneratedId gid = isGenerated ? meta.getIdField().getAnnotation(GeneratedId.class) : null;

        SQLData sqlData = generateSQL(entity, meta, isGenerated, gid);
        System.out.println(sqlData.getSql());

        try (PreparedStatement stmt = conn.prepareStatement(sqlData.getSql())) {
            for (int i = 0; i < sqlData.getValues().size(); i++) {
                stmt.setObject(i + 1, sqlData.getValues().get(i));
            }
            stmt.executeUpdate();
        }

        return true;
    }
    public static Boolean updateMany(Connection conn, EntityMeta meta, List entities) throws Exception {
        if (entities == null || entities.isEmpty()) return false;

        boolean isGenerated = meta.getIdField().isAnnotationPresent(GeneratedId.class);
        GeneratedId gid = isGenerated ? meta.getIdField().getAnnotation(GeneratedId.class) : null;

        SQLData sqlData = generateSQL(entities.get(0), meta, isGenerated, gid);
        String sql = sqlData.getSql();
        System.out.println(sql);

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Prepare batch
            for (Object entity : entities) {
                SQLData data = generateSQL(entity, meta, isGenerated, gid);

                // Bind values
                List<Object> values = data.getValues();
                for (int i = 0; i < values.size(); i++) {
                    stmt.setObject(i + 1, values.get(i));
                }
                stmt.addBatch();
            }

            // Execute batch
            stmt.executeBatch();

            // Handle auto-generated IDs (if needed)
            if (isGenerated && gid.strategy() == GenerationType.AUTO) {
                ResultSet keys = stmt.getGeneratedKeys();
                int index = 0;
                while (keys.next() && index < entities.size()) {
                    Object idValue = keys.getObject(1);
                    meta.getIdField().set(entities.get(index), idValue);
                    index++;
                }
            }
        }
        return true;
    }


    private static SQLData generateSQL(Object entity, EntityMeta meta, boolean isGenerated, GeneratedId gid) throws Exception {
        StringBuilder sql = new StringBuilder("UPDATE " + meta.getTableName() + " SET ");
        List<Object> values = new ArrayList<>();

        Field idField = meta.getIdField();
        idField.setAccessible(true);
        Object idValue = idField.get(entity);
        if (idValue == null) {
            throw new RuntimeException("Cannot update entity without ID value");
        }

        List<Field> columns = meta.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            Field field = columns.get(i);
            field.setAccessible(true);

            if (field.equals(idField)) continue; // skip ID in update

            Column column = field.getAnnotation(Column.class);
            sql.append(column.name()).append("=?");
            values.add(field.get(entity));

            if (i < columns.size() - 1) {
                sql.append(", ");
            }
        }

        sql.append(" WHERE ")
                .append(idField.getAnnotation(Column.class).name())
                .append(" = ?");
        values.add(idValue);

        return new SQLData(sql.toString(), values);
    }

}
