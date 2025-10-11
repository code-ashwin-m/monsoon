package org.monsoon.test.webtest;

import org.monsoon.framework.core.Monsoon;
import org.monsoon.framework.core.annotations.MonsoonApplication;
import org.monsoon.framework.core.context.ApplicationContext;

@MonsoonApplication
public class WebTest {

    public static void main(String[] args) throws Exception {
        ApplicationContext context = Monsoon.run(WebTest.class);
        context.refresh();
    }

}
