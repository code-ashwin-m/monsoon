package org.monsoon.framework.web.autoconfigure;

import org.monsoon.framework.web.interfaces.ViewRenderer;

import java.util.Map;

public class DefaultViewRenderer implements ViewRenderer {
    @Override
    public String render(Object templateName, Map<String, Object> model) {
        return templateName.toString();
    }
}
