package org.monsoon.framework.db;

import org.monsoon.framework.core.annotations.Property;

@Property("monsoon.datasource")
public class DataSourceProperty {
    private Boolean enabled;
    private String driver;
    private String path;
    private String database;
    private String url;
    private String username;
    private String password;
    private Boolean enforceForeignKeys;

    public Boolean isEnabled() {
        return enabled;
    }

    public String getDriver() {
        return driver;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getEnforceForeignKeys() {
        return enforceForeignKeys;
    }

    @Override
    public String toString() {
        return "DataSourceProperty{" +
                "enabled=" + enabled +
                ", driver='" + driver + '\'' +
                ", path='" + path + '\'' +
                ", database='" + database + '\'' +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", enforceForeignKeys=" + enforceForeignKeys +
                '}';
    }
}
