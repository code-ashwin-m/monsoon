package org.monsoon.framework.web;

import java.util.HashMap;
import java.util.Map;

public class Model {
    private Map<String, Object> attributes;
    public Model() {
        this.attributes = new HashMap<>();
    }
    public void addAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
