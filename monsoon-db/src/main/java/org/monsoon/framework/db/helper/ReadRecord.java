package org.monsoon.framework.db.helper;

import org.monsoon.framework.db.EntityMeta;
import org.monsoon.framework.db.annotations.Column;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReadRecord {
    public static Object execute(Connection conn, EntityMeta meta, String sql, Object[] args, Class<?> returnType) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    stmt.setObject(i + 1, args[i]);
                }
            }
            ResultSet rs = stmt.executeQuery();

            if (returnType.equals(List.class)) {
                List<Object> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(meta, rs));
                }
                return results;
            } else {
                if (rs.next()) {
                    return mapRow(meta, rs);
                }
                return null;
            }
        }
    }

    public static Object findAll(Connection conn, EntityMeta meta) throws Exception {
        SQLData sqlData = generateSQL(meta, null, null);
        String sql = sqlData.getSql();
        System.out.println(sql);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            List<Object> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapRow(meta, rs));
            }
            return results;
        }
    }

    public static Object findById(Connection conn, EntityMeta meta, Object id) throws Exception {
        SQLData sqlData = generateSQL(meta, meta.getIdField().getAnnotation(Column.class).name(), id);
        String sql = sqlData.getSql();
        System.out.println(sql);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(meta, rs);
            }
        }
        return null;
    }

    public static Object findByColumn(Connection conn, EntityMeta meta, String column, Object value) throws Exception {
        SQLData sqlData = generateSQL(meta, column, value);
        String sql = sqlData.getSql();
        System.out.println(sql);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, value);
            ResultSet rs = stmt.executeQuery();
            List<Object> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapRow(meta, rs));
            }
            return results;
        }
    }

    private static SQLData generateSQL(EntityMeta meta, String column, Object value) {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + meta.getTableName());
        List<Object> values = new ArrayList<>();

        Field idField = meta.getIdField();
        idField.setAccessible(true);

        if ( column != null ){
            sql.append(" WHERE ")
                    .append(column)
                    .append(" = ?");
            values.add(value);
        }
        return new SQLData(sql.toString(), null);
    }

    private static Object mapRow(EntityMeta meta, ResultSet rs) throws Exception {
        Object obj = meta.getEntityClass().getDeclaredConstructor().newInstance();
        for (Field field : meta.getColumns()) {
            field.setAccessible(true);
            field.set(obj, rs.getObject(field.getAnnotation(Column.class).name()));
        }
        return obj;
    }

}
