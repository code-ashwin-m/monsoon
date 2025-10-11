package org.monsoon.framework.db.helper;

import org.monsoon.framework.db.EntityMeta;
import org.monsoon.framework.db.annotations.Column;
import org.monsoon.framework.db.annotations.GeneratedId;
import org.monsoon.framework.db.enums.GenerationType;
import org.monsoon.framework.db.interfaces.IdGenerator;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CreateRecord {

    public static Boolean createOne(Connection conn, EntityMeta meta, Object entity) throws Exception {
        if (entity == null) return false;
        boolean isGenerated = meta.getIdField().isAnnotationPresent(GeneratedId.class);
        GeneratedId gid = isGenerated ? meta.getIdField().getAnnotation(GeneratedId.class) : null;

        SQLData sqlData = generateSQL(entity, meta, isGenerated, gid);
        System.out.println(sqlData.getSql());

        try (PreparedStatement stmt = conn.prepareStatement(sqlData.getSql(), Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < sqlData.getValues().size(); i++) {
                stmt.setObject(i + 1, sqlData.getValues().get(i));
            }
            stmt.executeUpdate();

            if (isGenerated && gid.strategy() == GenerationType.AUTO) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    Object idValue = keys.getObject(1);
                    meta.getIdField().set(entity, idValue);
                }
            }
        }
        return true;
    }

    public static Boolean createMany(Connection conn, EntityMeta meta, List entities) throws Exception {
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
        StringBuffer sql = new StringBuffer("INSERT INTO " + meta.getTableName() + " (");
        StringBuffer placeholders = new StringBuffer("VALUES (");

        List<Field> columns = meta.getColumns();
        Object idValue;
        List<Object> values = new ArrayList<>();

        for (int i = 0; i < columns.size(); i++) {
            Field field = columns.get(i);
            field.setAccessible(true);
            Column column = columns.get(i).getAnnotation(Column.class);
            String columnName = !column.name().isEmpty() ? column.name() : field.getName();

            if (field.equals(meta.getIdField()) && isGenerated){
                if (gid.strategy() == GenerationType.AUTO) {
                    continue; // DB will generate → skip inserting id
                }

                if (gid.strategy() == GenerationType.CUSTOM) {
                    IdGenerator gen = gid.generator().getConstructor().newInstance();
                    idValue = gen.generate();
                    meta.getIdField().set(entity, idValue);
                }
            }

            sql.append(columnName);
            placeholders.append("?");
            values.add(field.get(entity));

            if (i < columns.size() - 1) {
                sql.append(", ");
                placeholders.append(", ");
            }
        }

        sql.append(") ").append(placeholders).append(")");

        return new SQLData(sql.toString(), values);
    }

}
