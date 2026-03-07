package org.monsoon.framework.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionFactory {
    public static Connection getConnection(DataSourceProperty dataSource) throws Exception {
        if (TransactionManager.isTransactionActive()) {
            return TransactionManager.getCurrentConnection();
        }

        return DriverManager.getConnection(
                dataSource.getUrl(),
                dataSource.getUsername(),
                dataSource.getPassword()
        );
    }
}
