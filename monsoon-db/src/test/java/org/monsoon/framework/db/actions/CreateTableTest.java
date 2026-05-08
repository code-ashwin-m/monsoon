package org.monsoon.framework.db.actions;

import org.junit.Test;
import org.monsoon.framework.db.EntityMeta;
import org.monsoon.framework.db.annotations.Column;
import org.monsoon.framework.db.annotations.Entity;
import org.monsoon.framework.db.annotations.Id;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CreateTableTest {

    @Test
    public void testGenerateSQL_SQLite() throws Exception {
        EntityMeta meta = createMeta(TestEntity.class);
        SQLData sqlData = CreateTable.generateSQL(meta, "sqlite");
        String sql = sqlData.getSql().toUpperCase();

        // Check basic structure
        assertTrue(sql.startsWith("CREATE TABLE IF NOT EXISTS TEST_ENTITIES"));

        // Check types for SQLite
        assertTrue(sql.contains("ID INTEGER"));
        assertTrue(sql.contains("NAME TEXT"));
        assertTrue(sql.contains("AGE INTEGER"));
        assertTrue(sql.contains("ACTIVE INTEGER"));
        assertTrue(sql.contains("SALARY REAL"));
        assertTrue(sql.contains("BIG_VALUE REAL"));

        // Check constraints
        assertTrue(sql.contains("NAME TEXT NOT NULL DEFAULT \"JOHN DOE\""));
        assertTrue(sql.contains("UNIQUE(AGE)"));
        assertTrue(sql.contains("UNIQUE(COMBO1, COMBO2)"));
        assertTrue(sql.contains("PRIMARY KEY (ID AUTOINCREMENT)"));

        // Check foreign keys
        assertTrue(sql.contains("FOREIGN KEY(PARENT_ID) REFERENCES PARENT_ENTITIES(ID) ON DELETE CASCADE"));
    }

    @Test
    public void testGenerateSQL_Generic() throws Exception {
        EntityMeta meta = createMeta(TestEntity.class);
        SQLData sqlData = CreateTable.generateSQL(meta, "mysql");
        String sql = sqlData.getSql().toUpperCase();

        // Check basic structure
        assertTrue(sql.startsWith("CREATE TABLE IF NOT EXISTS TEST_ENTITIES"));

        // Check types for MySQL/Generic
        assertTrue(sql.contains("ID INT"));
        assertTrue(sql.contains("NAME VARCHAR(255)"));
        assertTrue(sql.contains("AGE INT"));
        assertTrue(sql.contains("ACTIVE BOOLEAN"));
        assertTrue(sql.contains("SALARY DOUBLE"));
        assertTrue(sql.contains("BIG_VALUE BIGINT"));

        // Check constraints
        assertTrue(sql.contains("NAME VARCHAR(255) NOT NULL DEFAULT \"JOHN DOE\""));
        assertTrue(sql.contains("UNIQUE(AGE)"));
        assertTrue(sql.contains("UNIQUE(COMBO1, COMBO2)"));
        assertTrue(sql.contains("PRIMARY KEY (ID)")); // Note: Generic doesn't add AUTOINCREMENT in this implementation

        // Check foreign keys
        assertTrue(sql.contains("FOREIGN KEY(PARENT_ID) REFERENCES PARENT_ENTITIES(ID) ON DELETE CASCADE"));
    }

    @Test
    public void testExecution_SQLite() throws Exception {
        EntityMeta parentMeta = createMeta(ParentEntity.class);
        EntityMeta testMeta = createMeta(TestEntity.class);

        Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");

        assertTrue(CreateTable.createTableIfNotExists(conn, parentMeta));
        assertTrue(CreateTable.createTableIfNotExists(conn, testMeta));
    }

    @Test(expected = SQLException.class)
    public void testExecution_ErrorSyntaxError() throws Exception {
        EntityMeta badMeta = createMeta(SingleQuoteTableEntity.class);
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            CreateTable.createTableIfNotExists(conn, badMeta);
        }
    }

    @Test(expected = SQLException.class)
    public void testExecution_ErrorClosedConnection() throws Exception {
        EntityMeta meta = createMeta(ParentEntity.class);
        Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        conn.close();
        CreateTable.createTableIfNotExists(conn, meta);
    }

    private EntityMeta createMeta(Class<?> entityClass) {
        Entity entityAnn = entityClass.getAnnotation(Entity.class);
        String tableName = entityAnn.tableName();
        Field idField = null;
        List<Field> columns = new ArrayList<>();
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                columns.add(field);
                if (field.isAnnotationPresent(Id.class)) {
                    idField = field;
                }
            }
        }
        return new EntityMeta(entityClass, tableName, idField, columns);
    }
}
