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

    public static Object createOne(Connection conn, EntityMeta meta, Object entity) throws Exception {
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

    public static Object createMany(Connection conn, EntityMeta meta, Object entities) throws Exception {
        for (Object entity: (List) entities){
            createOne(conn, meta, entity);
        }
        return true;
    }

    private static SQLData generateSQL(Object entity, EntityMeta meta, boolean isGenerated, GeneratedId gid) throws Exception {
        StringBuffer sql = new StringBuffer("INSERT INTO " + meta.getTableName() + " (");
        StringBuffer placeholders = new StringBuffer("VALUES (");

        List<Field> columns = meta.getColumns();
        Object idValue = null;
        List<Object> values = new ArrayList<>();

        for (int i = 0; i < columns.size(); i++) {
            columns.get(i).setAccessible(true);
            Column column = columns.get(i).getAnnotation(Column.class);


            if (columns.get(i).equals(meta.getIdField()) && isGenerated){
                if (gid.strategy() == GenerationType.AUTO) {
                    continue; // DB will generate → skip inserting id
                }

                if (gid.strategy() == GenerationType.CUSTOM) {
                    IdGenerator gen = gid.generator().getConstructor().newInstance();
                    idValue = gen.generate();
                    meta.getIdField().set(entity, idValue);
                }
            }

            sql.append(column.name());
            placeholders.append("?");
            values.add(columns.get(i).get(entity));

            if (i < columns.size() - 1) {
                sql.append(", ");
                placeholders.append(", ");
            }
        }

        sql.append(") ").append(placeholders).append(")");

        return new SQLData(sql.toString(), values);
    }

}
