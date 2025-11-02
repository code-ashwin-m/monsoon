package org.monsoon.framework.web;

import javax.servlet.Filter;

public class FilterRegistration {
    private Filter filter;
    private Class<?> filterClass;
    private int order;
    private String[] pattern;

    public FilterRegistration(Filter filter, Class<?> filterClass, int i, String... pattern) {
        this.filter = filter;
        this.filterClass = filterClass;
        this.order = order;
        this.pattern = pattern;
    }

    public Filter getFilter() {
        return filter;
    }

    public Class<?> getFilterClass() {
        return filterClass;
    }

    public int getOrder() {
        return order;
    }

    public String[] getPattern() {
        return pattern;
    }
}
