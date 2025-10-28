package org.monsoon.framework.web.autoconfigure;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.monsoon.framework.core.annotations.Bean;
import org.monsoon.framework.core.annotations.ConditionalOnClass;
import org.monsoon.framework.core.annotations.ConditionalOnMissingBean;
import org.monsoon.framework.web.ServletWebAdapter;
import org.monsoon.framework.web.interfaces.EmbeddedServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to configure the embedded Tomcat server.
 * It is annotated with @ConditionalOnClass to ensure that the required classes are present.
 */
@ConditionalOnClass({javax.servlet.Servlet.class, javax.servlet.http.HttpServlet.class, org.apache.catalina.startup.Tomcat.class})
@ConditionalOnMissingBean(EmbeddedServer.class)
public class TomcatAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(TomcatAutoConfiguration.class.getName());

    /**
     * Returns a new instance of TomcatEmbeddedServer.
     *
     * @return a new instance of TomcatEmbeddedServer
     */
    @Bean
    public TomcatEmbeddedServer embeddedServer() {
        return new TomcatEmbeddedServer();
    }

    public class TomcatEmbeddedServer implements EmbeddedServer {

        /**
         * Starts the embedded Tomcat server.
         *
         * @param host the host name
         * @param port the port number
         * @param servlet the servlet to be used
         * @throws Exception if an error occurs while starting the server
         */
        @Override
        public void start(String host, Integer port, ServletWebAdapter servlet) throws Exception {
            logger.debug("Starting embedded Tomcat...");
            Tomcat tomcat = new Tomcat();
            tomcat.setPort(port);
            tomcat.getConnector();

            Context context = tomcat.addContext("", null);
            tomcat.addServlet(context, "dispatcher", servlet);
            context.addServletMappingDecoded("/*", "dispatcher");

            tomcat.start();
            logger.debug("Tomcat started on {}:{}", host, port);
            tomcat.getServer().await();
        }
    }
}
