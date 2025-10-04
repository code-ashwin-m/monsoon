package org.ashwin.monsoon.config;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.ashwin.monsoon.config.test1.DatabaseConfig;
import org.ashwin.monsoon.config.test1.ServerConfig;
import org.ashwin.monsoon.config.test1.ServerListConfig;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

    public AppTest() {}

    public void testConfiguration() {
        String value = Configuration.get("db.url");
        System.out.println(value);
        assertEquals("jdbc:sqlite:test.db", value);
    }

    public void testConfigurationPropertiesAnn(){
        DatabaseConfig databaseConfig = ConfigurationBinder.bind(DatabaseConfig.class);
        System.out.println(databaseConfig.getUrl());
        assertEquals("jdbc:sqlite:test.db", databaseConfig.getUrl());
    }

    public void  testNestedConfigurationProperties(){
        ServerConfig serverConfig = ConfigurationBinder.bind(ServerConfig.class);
        System.out.println(serverConfig.getSsl().getKeyStore());
        assertEquals("mykeystore.jks", serverConfig.getSsl().getKeyStore());
    }

    public void testConfigurationArray() {
        ServerListConfig serverListConfig = ConfigurationBinder.bind(ServerListConfig.class);

        for (ServerListConfig.ServerConfig config : serverListConfig.getServers()){
            System.out.println("host: " + config.getHost());
            System.out.println("port: " + config.getPort());
            System.out.println("tag: " + config.getTag());
        }
        assertEquals("localhost", serverListConfig.getServers().get(0).getHost());
    }
}
