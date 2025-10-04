package org.ashwin.monsoon.config.test1;

import org.ashwin.monsoon.config.annotations.ConfigurationProperties;
import org.ashwin.monsoon.config.annotations.Value;

@ConfigurationProperties(prefix = "db")
public class DatabaseConfig {
    private String driver;
    private String url;
    @Value("${username:root}")
    private String username;
    private String password;

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
