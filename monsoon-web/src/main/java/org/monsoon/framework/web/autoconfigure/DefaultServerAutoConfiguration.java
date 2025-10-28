package org.monsoon.framework.web.autoconfigure;

import org.monsoon.framework.core.annotations.Bean;
import org.monsoon.framework.core.annotations.ConditionalOnMissingBean;
import org.monsoon.framework.web.ServletWebAdapter;
import org.monsoon.framework.web.interfaces.EmbeddedServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ConditionalOnMissingBean(EmbeddedServer.class)
public class DefaultServerAutoConfiguration  {
    private static final Logger logger = LoggerFactory.getLogger(DefaultServerAutoConfiguration.class);
    @Bean
    public DefaultEmbeddedServer embeddedServer() {
        return new DefaultEmbeddedServer();
    }

    public class DefaultEmbeddedServer implements EmbeddedServer {
        @Override
        public void start(String host, Integer port, ServletWebAdapter servlet) throws Exception {
            logger.error("DefaultEmbeddedServer, no server dependencies found");
        }
    }
}
