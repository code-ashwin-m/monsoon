package org.monsoon.framework;

import junit.framework.TestCase;
import org.monsoon.framework.core.context.ApplicationContext;
import org.monsoon.framework.core.Monsoon;
import org.monsoon.framework.core.configuration.MonsoonApplication;

@MonsoonApplication
public class AppTest extends TestCase {

    public void testApp() throws Exception {
        String[] args = {};
        ApplicationContext context = Monsoon.run(AppTest.class, args);
        System.out.println(context.getClasses());
    }
}
