package org.monsoon.sample.webserver;

import org.monsoon.framework.core.Monsoon;
import org.monsoon.framework.core.annotations.MonsoonApplication;
import org.monsoon.framework.core.interfaces.ApplicationContext;
import org.monsoon.framework.core.properties.ApplicationProperties;
import org.monsoon.sample.App;

@MonsoonApplication
public class WebServer {
    public static void main(String[] args) throws Exception {
        ApplicationContext context = Monsoon.run(App.class, args);
        String appName = ApplicationProperties.get("app.name", "Monsoon");

        context.refresh();
    }
}
