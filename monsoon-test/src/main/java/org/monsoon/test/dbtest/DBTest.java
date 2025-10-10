package org.monsoon.test.dbtest;

import org.monsoon.framework.core.Monsoon;
import org.monsoon.framework.core.annotations.MonsoonApplication;
import org.monsoon.framework.core.context.ApplicationContext;

@MonsoonApplication
public class DBTest {
    public static void main(String[] args) throws Exception {
        ApplicationContext context = Monsoon.run(DBTest.class);

        TestService service = context.getBean(TestService.class);
        service.update();
    }
}
