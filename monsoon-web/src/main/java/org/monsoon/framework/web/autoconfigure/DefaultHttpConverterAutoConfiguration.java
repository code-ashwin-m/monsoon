package org.monsoon.framework.web.autoconfigure;

import org.monsoon.framework.core.annotations.Bean;
import org.monsoon.framework.core.annotations.ConditionalOnMissingClass;
import org.monsoon.framework.web.interfaces.HttpMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

@ConditionalOnMissingClass(HttpMessageConverter.class)
public class DefaultHttpConverterAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(DefaultHttpConverterAutoConfiguration.class);
    @Bean
    public HttpMessageConverter httpMessageConverter() {
        return new DefaultHttpMessageConverter();
    }

    public class DefaultHttpMessageConverter implements HttpMessageConverter {
        @Override
        public Object readValue(InputStream bodyStream, Class<?> type) throws Exception {
            return type.getConstructor().newInstance();
        }

        @Override
        public String writeValueAsString(Object result) throws Exception {
            return "";
        }
    }
}
