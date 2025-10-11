package org.monsoon.test.coretest;

import org.monsoon.framework.core.Monsoon;
import org.monsoon.framework.core.annotations.MonsoonApplication;
import org.monsoon.framework.core.configuration.ConfigurationBinder;
import org.monsoon.framework.core.context.ApplicationContext;

//@MonsoonApplication
public class App 
{
    public static void main( String[] args ) throws Exception {
        ApplicationContext context = Monsoon.run(App.class, args);

        TestComponent component1 = context.getBean("testComponent", TestComponent.class);
        TestComponent component2 = context.getBean("testComponent", TestComponent.class);

        System.out.println(component1);
        System.out.println(component2);

        System.out.println(component1.getSingleton());
        System.out.println(component2.getSingleton());

        TestComponentSingleton singleton1 = context.getBean("comp1", TestComponentSingleton.class);
        TestComponentSingleton singleton2 = context.getBean("comp1", TestComponentSingleton.class);

        System.out.println(singleton1);
        System.out.println(singleton2);

        TestComponentSingleton singleton3 = Monsoon.getInstance().getContext().getBean("comp1", TestComponentSingleton.class);
        System.out.println(singleton3);

        TestDatabaseConfig dbConfig = ConfigurationBinder.bind(TestDatabaseConfig.class);
        System.out.println(dbConfig.getDriver());
        System.out.println(dbConfig.getUrl());
        System.out.println(dbConfig.getUsername());
        System.out.println(dbConfig.getPassword());

        TestServerConfig serverConfig = ConfigurationBinder.bind(TestServerConfig.class);
        System.out.println(serverConfig.getHost());
        System.out.println(serverConfig.getPort());
        System.out.println(serverConfig.getSsl().isEnabled());
        System.out.println(serverConfig.getSsl().getKeyStore());

        TestServerListConfig serverListConfig = ConfigurationBinder.bind(TestServerListConfig.class);
        System.out.println(serverListConfig.getServers().size());
        for (TestServerListConfig.ServerConfig server : serverListConfig.getServers()) {
            System.out.println(server.getHost());
            System.out.println(server.getPort());
        }
    }
}
