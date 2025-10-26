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
public class MonsoonTest {
    private static final Logger logger = LoggerFactory.getLogger(MonsoonTest.class);
    public static void main(String[] args) throws Exception {
        ApplicationContext context = MonsoonApplication.run(MonsoonTest.class, args);

        TestComponent object = context.getBean("testComp", TestComponent.class);
        logger.debug("response from bean object: -> " + object.process());

        String appName = ApplicationProperties.get("app.name", "Monsoon");

        logger.info("Application name: {}", appName);
    }
}
