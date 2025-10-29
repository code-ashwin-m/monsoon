package org.monsoon.framework.web.autoconfigure;

import org.monsoon.framework.core.annotations.Bean;
import org.monsoon.framework.core.annotations.ConditionalOnMissingBean;
import org.monsoon.framework.web.interfaces.EmbeddedServer;
import org.monsoon.framework.web.interfaces.HttpMessageConverter;
import org.monsoon.framework.web.interfaces.ViewRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@ConditionalOnMissingBean(ViewRenderer.class)
public class DefaultViewRendererAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(DefaultViewRendererAutoConfiguration.class);

    @Bean
    public ViewRenderer viewRenderer() {
        return new DefaultViewRenderer();
    }

    public class DefaultViewRenderer implements ViewRenderer {
        @Override
        public String render(Object templateName, Map<String, Object> model) {
            return templateName.toString();
        }
    }

}
