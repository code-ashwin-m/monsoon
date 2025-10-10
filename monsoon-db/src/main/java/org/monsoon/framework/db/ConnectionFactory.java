package org.monsoon.framework.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionFactory {
    private static Connection connection;

    public static Connection getConnection(DatabaseConfiguration dbConfig) throws Exception {
        if (connection == null || connection.isClosed()) {
            Class.forName(dbConfig.getDriver());
            connection = DriverManager.getConnection(dbConfig.getUrl(), dbConfig.getUsername(), dbConfig.getPassword());
        }
        return connection;
    }
}
