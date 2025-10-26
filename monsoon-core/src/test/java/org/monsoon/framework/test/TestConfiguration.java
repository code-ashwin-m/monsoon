package org.monsoon.framework.test;

import org.monsoon.framework.core.annotations.ConditionalOnClass;
import org.monsoon.framework.core.annotations.Configuration;

@Configuration
@ConditionalOnClass(name = "org.monsoon.framework.web.ApplicationContextFromWeb")
public class TestConfiguration {
}
