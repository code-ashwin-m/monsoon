package org.monsoon.framework.test;

import org.monsoon.framework.core.MonsoonApplication;
import org.monsoon.framework.core.annotations.ComponentScan;
import org.monsoon.framework.core.annotations.Configuration;
import org.monsoon.framework.core.annotations.EnableAutoConfiguration;
import org.monsoon.framework.core.interfaces.ApplicationContext;
import org.monsoon.framework.core.properties.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class WebAppTest {
    private static final Logger logger = LoggerFactory.getLogger(WebAppTest.class);
    public static void main(String[] args) throws Exception {

        ApplicationContext context = MonsoonApplication.run(WebAppTest.class, args);
        String appName = ApplicationProperties.get("app.name", "Monsoon");

        logger.info("Application name: {}", appName);
        context.refresh();

    }
}
