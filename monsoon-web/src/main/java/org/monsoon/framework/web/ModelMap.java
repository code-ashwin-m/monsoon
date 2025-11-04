package org.monsoon.framework.web;

import java.util.HashMap;
import java.util.Map;

public class ModelMap {
    private Map<String, Object> attributes;
    public ModelMap() {
        this.attributes = new HashMap<>();
    }
    public void addAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
