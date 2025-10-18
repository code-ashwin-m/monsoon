package org.monsoon.framework;

import org.monsoon.framework.core.annotations.Bean;
import org.monsoon.framework.core.annotations.Configuration;

@Configuration
public class TestConfiguration {
    @Bean
    public SampleModal sampleModal() {
        return new SampleModal("hello");
    }
}
