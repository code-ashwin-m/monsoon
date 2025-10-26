package org.monsoon.framework.web.interfaces;

import java.util.Map;

public interface ViewRenderer {
    String render(Object templateName, Map<String, Object> model);
}
