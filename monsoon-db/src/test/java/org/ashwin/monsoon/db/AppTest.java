package org.ashwin.monsoon.db;

import junit.framework.TestCase;
import org.ashwin.monsoon.config.ConfigurationBinder;
import org.ashwin.monsoon.core.context.ApplicationContext;
import org.ashwin.monsoon.core.annotations.ComponentScan;
import org.ashwin.monsoon.db.test1.DbConfig;
import org.ashwin.monsoon.db.test1.TestEntity;
import org.ashwin.monsoon.db.test1.TestService;

@ComponentScan("org.ashwin.monsoon.db")
public class AppTest extends TestCase {

    public AppTest(  ) {}

    public void testApp() throws Exception {
        ApplicationContext context = new ApplicationContext(AppTest.class);
        DbConfig dbConfig = ConfigurationBinder.bind(DbConfig.class);
        context.addBeanPostProcessor(new RepositoryPostProcessor(dbConfig));
        TestService service = context.getBean(TestService.class);
        service.createTableIfNotExists();
        System.out.println("id : " + service.create(new TestEntity()));
    }
}
