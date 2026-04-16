package org.monsoon.framework.db.autoconfigure;
import org.monsoon.framework.core.Monsoon;
import org.monsoon.framework.core.annotations.AutoConfigureAfter;
import org.monsoon.framework.core.annotations.Bean;
import org.monsoon.framework.core.annotations.ConditionalOnProperty;
import org.monsoon.framework.core.properties.PropertyBinder;
import org.monsoon.framework.db.DataSource;
import org.monsoon.framework.db.DataSourceProperty;
import org.monsoon.framework.db.RepositoryBeanPostProcessor;
import org.monsoon.framework.db.TransactionBeanPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoConfigureAfter(name = {"org.monsoon.framework.core.autoconfigure.YamlAutoConfiguration"})
@ConditionalOnProperty({"monsoon.datasource.enabled"})
public class DataSourceAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceAutoConfiguration.class);

    @Bean
    public DataSource dataSource(){
        DataSourceProperty dataSourceProperty = PropertyBinder.bind(DataSourceProperty.class);
        if (dataSourceProperty.isEnabled()) {
            if (dataSourceProperty.getUrl() !=null && !dataSourceProperty.getUrl().isEmpty()){
                DataSource dataSource = new DataSource(dataSourceProperty);
                Monsoon.getContext().registerBeanPostProcessor(new TransactionBeanPostProcessor(dataSource));
                Monsoon.getContext().registerBeanPostProcessor(new RepositoryBeanPostProcessor(dataSource));
                return dataSource;
            }
        }else {
            logger.debug("Datasource is disabled, please check property: monsoon.datasource.enabled");
        }
        return null;
    }
}
