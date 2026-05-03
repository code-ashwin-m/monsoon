package org.monsoon.framework.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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

    public Connection getConnection() throws Exception {
        if (connection == null || connection.isClosed()) {
            Class.forName(dataSourceProperty.getDriver());

            String url = dataSourceProperty.getUrl() + ":";

            if (dataSourceProperty.getPath() != null && !dataSourceProperty.getPath().isEmpty()) {
                if (!dataSourceProperty.getPath().endsWith(File.separator)){
                    dataSourceProperty.setPath(dataSourceProperty.getPath() + File.separator);
                }
                url += dataSourceProperty.getPath();
            }

            url += dataSourceProperty.getDatabase();

            logger.debug("Creating new connection to: {}", url);

            connection = DriverManager.getConnection(url, dataSourceProperty.getUsername(),
                    dataSourceProperty.getPassword());

            if (dataSourceProperty.getEnforceForeignKeys()) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                    logger.debug("PRAGMA foreign_keys is enabled");
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
