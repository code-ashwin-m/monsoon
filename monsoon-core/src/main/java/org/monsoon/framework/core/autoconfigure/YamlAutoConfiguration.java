package org.monsoon.framework.core.autoconfigure;

import org.monsoon.framework.core.annotations.Bean;
import org.monsoon.framework.core.annotations.ConditionalOnClass;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

@ConditionalOnClass(name = "org.yaml.snakeyaml.Yaml")
public class YamlAutoConfiguration {

    @Bean
    public YamlHelper yamlHelper() {
        return new YamlHelper();
    }

    public class YamlHelper{

        public Map<String, Object> load(InputStream input) {
            Yaml yaml = new Yaml();
            Map<String, Object> map = yaml.load(input);
            return map;
        }
    }
}
