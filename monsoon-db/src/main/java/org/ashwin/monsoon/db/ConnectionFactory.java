package org.ashwin.monsoon.db;

import org.ashwin.monsoon.db.interfaces.BaseDatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;


public class ConnectionFactory {
    private static Connection connection;

    public static Connection getConnection(BaseDatabaseConfig dbConfig) throws Exception {
        if (connection == null || connection.isClosed()) {
            Class.forName(dbConfig.getDriver());
            connection = DriverManager.getConnection(dbConfig.getUrl(), dbConfig.getUsername(), dbConfig.getPassword());
        }
        return connection;
    }
}
