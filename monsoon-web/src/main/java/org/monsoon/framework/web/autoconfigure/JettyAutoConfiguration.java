package org.monsoon.framework.web.autoconfigure;

import org.eclipse.jetty.servlet.ServletHolder;
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
@ConditionalOnClass(org.eclipse.jetty.server.Server.class)
@ConditionalOnMissingBean(EmbeddedServer.class)
public class JettyAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(JettyAutoConfiguration.class.getName());


    @Bean
    public JettyEmbeddedServer embeddedServer() {
        return new JettyEmbeddedServer();
    }

    public class JettyEmbeddedServer implements EmbeddedServer {

        /**
         * Starts the embedded Jetty server.
         *
         * @param host the host name
         * @param port the port number
         * @param servlet the servlet to be used
         * @throws Exception if an error occurs while starting the server
         */
        @Override
        public void start(String host, Integer port, ServletWebAdapter servlet) throws Exception {
            logger.debug("Starting embedded Jetty...");

            org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(port);

            org.eclipse.jetty.servlet.ServletContextHandler context = new org.eclipse.jetty.servlet.ServletContextHandler(org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);

            ServletHolder holder = new ServletHolder("dispatcher", servlet);
            context.addServlet(holder, "/*");

            server.start();
            logger.debug("Jetty started on {}:{}", host, port);
            server.join();

        }
    }
}
