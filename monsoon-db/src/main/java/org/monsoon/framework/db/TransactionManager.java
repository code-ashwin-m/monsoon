package org.monsoon.framework.db;

import java.sql.Connection;

public class TransactionManager {

    private static final ThreadLocal<Connection> CONNECTION = new ThreadLocal<>();

    public static void begin(Connection conn) throws Exception {
        conn.setAutoCommit(false);
        CONNECTION.set(conn);
    }

    public static void commit() throws Exception {
        Connection conn = CONNECTION.get();
        if (conn != null) {
            conn.commit();
            conn.close();
            CONNECTION.remove();
        }
    }

    public static void rollback() throws Exception {
        Connection conn = CONNECTION.get();
        if (conn != null) {
            conn.rollback();
            conn.close();
            CONNECTION.remove();
        }
    }

    public static Connection getCurrentConnection() {
        return CONNECTION.get();
    }

    public static boolean isTransactionActive() {
        return CONNECTION.get() != null;
    }
}