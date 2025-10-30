package org.monsoon.framework.db.autoconfigure;

import org.monsoon.framework.core.annotations.AutoConfigureAfter;
import org.monsoon.framework.core.annotations.ConditionalOnProperty;

@AutoConfigureAfter(name = {"org.monsoon.framework.core.autoconfigure.YamlAutoConfiguration"})
@ConditionalOnProperty({"monsoon.datasource.enabled"})
public class DataSourceAutoConfiguration {
}
