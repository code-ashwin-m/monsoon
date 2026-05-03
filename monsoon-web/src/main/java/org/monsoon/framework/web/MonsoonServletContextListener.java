package org.monsoon.framework.web;

import org.monsoon.framework.core.annotations.ComponentScan;
import org.monsoon.framework.core.annotations.Configuration;
import org.monsoon.framework.core.annotations.EnableAutoConfiguration;
import org.monsoon.framework.core.annotations.MonsoonApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.*;
import javax.servlet.annotation.HandlesTypes;

@HandlesTypes({
        MonsoonApplication.class,
        Configuration.class,
        ComponentScan.class,
        EnableAutoConfiguration.class
})
public class MonsoonServletContextListener implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(MonsoonServletContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.info("Monsoon ServletContext initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == classLoader) {
                try {
                    DriverManager.deregisterDriver(driver);
                    logger.info("Deregistered driver: {}", driver.getClass().getName());
                } catch (SQLException e) {
                    logger.error("Failed to deregister driver: {}", driver.getClass().getName(), e);
                }
            }
        }

        logger.info("Monsoon ServletContext destroyed");
    }
}
