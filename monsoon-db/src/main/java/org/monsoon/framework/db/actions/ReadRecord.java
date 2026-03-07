package org.monsoon.framework.db.actions;

import org.monsoon.framework.db.EntityMeta;
import org.monsoon.framework.db.annotations.Column;
import org.monsoon.framework.db.interfaces.DataPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReadRecord {
    private static final Logger logger = LoggerFactory.getLogger(ReadRecord.class);
    public static Object execute(Connection conn, EntityMeta meta, String sql, Object[] args, Class<?> returnType) throws Exception {
        logger.debug(sql);
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
        logger.debug(sql);

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
        logger.debug(sql);

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
        logger.debug(sql);

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
            Column column = field.getAnnotation(Column.class);
            String columnName = !column.name().isEmpty() ? column.name() : field.getName();

            DataPersister<?> convertor = Utils.resolveConvertor(field);
            Object dbValue = convertor.sqlToJava(rs.getObject(columnName));

            if (field.getType() == Boolean.class || field.getType() == boolean.class) {
                if (dbValue instanceof Number) {
                    dbValue = ((Number) dbValue).intValue() != 0;
                }
            }
            field.set(obj, dbValue);
        }
        return obj;
    }
}
