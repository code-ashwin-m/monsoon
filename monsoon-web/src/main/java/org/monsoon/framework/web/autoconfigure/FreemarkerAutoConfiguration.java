package org.monsoon.framework.web.autoconfigure;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.monsoon.framework.core.annotations.AutoConfigureBefore;
import org.monsoon.framework.core.annotations.Bean;
import org.monsoon.framework.core.annotations.ConditionalOnClass;
import org.monsoon.framework.web.interfaces.ViewRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@ConditionalOnClass(name = "freemarker.template.Configuration")
@AutoConfigureBefore(DefaultViewRendererAutoConfiguration.class)
public class FreemarkerAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(FreemarkerAutoConfiguration.class);
    @Bean
    public ViewRenderer viewRenderer() {
        return new FreemarkerViewRenderer();
    }

    public class FreemarkerViewRenderer implements ViewRenderer {
        private Configuration cfg = null;
        public FreemarkerViewRenderer() {
            cfg = new Configuration(Configuration.VERSION_2_3_32);
            cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "/templates");
            cfg.setDefaultEncoding("UTF-8");
        }


        @Override
        public String render(Object templateName, Map<String, Object> model) {
            try {
                Template template = cfg.getTemplate(templateName + ".ftl");
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                template.process(model, writer);

                return out.toString();
            } catch (IOException e) {
                logger.error("Error while loading template {}", templateName, e);
            } catch (TemplateException e) {
                logger.error("Error while rendering template {}", templateName, e);
            }
            return null;
        }
    }
}
