package org.ashwin.monsoon.db.test1;

import org.ashwin.monsoon.config.annotations.ConfigurationProperties;
import org.ashwin.monsoon.db.interfaces.BaseDatabaseConfig;

@ConfigurationProperties(prefix = "db")
public class DbConfig implements BaseDatabaseConfig {
    private String url;
    private String username;
    private String password;
    private String driver;

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getDriver() {
        return driver;
    }
}
