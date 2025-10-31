package org.monsoon.framework.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class DataSource {
    private DataSourceProperty dataSourceProperty;
    private Connection connection;

    public DataSource(DataSourceProperty dataSourceProperty) {
        this.dataSourceProperty = dataSourceProperty;
    }

    public Connection getConnection() throws Exception{
        if (connection == null || connection.isClosed()) {
            Class.forName(dataSourceProperty.getDriver());
            connection = DriverManager.getConnection(dataSourceProperty.getUrl(), dataSourceProperty.getUsername(), dataSourceProperty.getPassword());
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
