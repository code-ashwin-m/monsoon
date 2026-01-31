package org.monsoon.framework.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionFactory {
    private static Connection connection;

    public static Connection getConnection(DataSourceProperty dataSource) throws Exception {
        if (connection == null || connection.isClosed()) {
            Class.forName(dataSource.getDriver());
            connection = DriverManager.getConnection(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
        }
        return connection;
    }
}
