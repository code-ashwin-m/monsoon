package org.monsoon.framework.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DataSource {
    private static final Logger logger = LoggerFactory.getLogger(DataSource.class);
    private final DataSourceProperty dataSourceProperty;
    private Connection connection;

    public DataSource(DataSourceProperty dataSourceProperty) {
        this.dataSourceProperty = dataSourceProperty;
    }

    public Connection getConnection() throws Exception{
        if (connection == null || connection.isClosed()) {
            Class.forName(dataSourceProperty.getDriver());
            connection = DriverManager.getConnection(dataSourceProperty.getUrl(), dataSourceProperty.getUsername(), dataSourceProperty.getPassword());

            if (dataSourceProperty.getEnforceForeignKeys()){
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                    logger.debug("PRAGMA is enabled");
                }
            }
        }
        return connection;
    }

    public DataSourceProperty getDataSourceProperty() {
        return dataSourceProperty;
    }

    @Override
    public String toString() {
        return "DataSource{" +
                "dataSourceProperty=" + dataSourceProperty +
                ", connection=" + connection +
                '}';
    }
}
