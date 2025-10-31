package org.monsoon.framework.db;

import org.monsoon.framework.core.interfaces.BeanPostProcessor;

public class RepositoryPostProcessor implements BeanPostProcessor {
    private DataSourceProperty dataSource;
    public RepositoryPostProcessor(DataSourceProperty dataSource){
        this.dataSource = dataSource;

    }
    @Override
    public Object postProcess(Class<?> clazz) {
        return null;
    }
}
