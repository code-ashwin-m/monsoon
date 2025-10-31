package org.monsoon.framework.web.autoconfigure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.monsoon.framework.core.annotations.AutoConfigureBefore;
import org.monsoon.framework.core.annotations.Bean;
import org.monsoon.framework.core.annotations.ConditionalOnClass;
import org.monsoon.framework.web.interfaces.HttpMessageConverter;

import java.io.InputStream;

@ConditionalOnClass(name = "com.fasterxml.jackson.databind.ObjectMapper")
@AutoConfigureBefore(DefaultHttpConverterAutoConfiguration.class)
public class JacksonAutoConfiguration {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * Creates a new instance of the Jackson ObjectMapper.
     * This is used to convert JSON to and from Java objects.
     * @return a new instance of the Jackson ObjectMapper
     */

    @Bean
    public HttpMessageConverter httpMessageConverter() {
        return new JacksonHelper();
    }

    public class JacksonHelper implements HttpMessageConverter {
        @Override
        public Object readValue(InputStream bodyStream, Class<?> type) throws Exception {
            return objectMapper.readValue(bodyStream, type);
        }

        @Override
        public String writeValueAsString(Object result) throws Exception {
            return objectMapper.writeValueAsString(result);
        }
    }
}
