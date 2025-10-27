package org.monsoon.sample;


import org.monsoon.framework.core.Monsoon;
import org.monsoon.framework.core.annotations.MonsoonApplication;
import org.monsoon.framework.core.interfaces.ApplicationContext;
import org.monsoon.framework.core.properties.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MonsoonApplication
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    public static void main(String[] args) throws Exception {

        ApplicationContext context = Monsoon.run(App.class, args);
        String appName = ApplicationProperties.get("app.name", "Monsoon");

        logger.info("Application name: {}", appName);
        context.refresh();
    }
}
