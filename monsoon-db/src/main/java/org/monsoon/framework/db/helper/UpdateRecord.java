package org.monsoon.framework.db.helper;

import org.monsoon.framework.db.EntityMeta;
import org.monsoon.framework.db.annotations.Column;
import org.monsoon.framework.db.annotations.GeneratedId;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class UpdateRecord {
    public static Object updateOne(Connection conn, EntityMeta meta, Object entity) throws Exception {
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
    public static Object updateMany(Connection conn, EntityMeta meta, Object entities) throws Exception {
        for (Object entity: (List) entities){
            updateOne(conn, meta, entity);
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
