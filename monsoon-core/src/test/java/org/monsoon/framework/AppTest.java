package org.monsoon.framework;

import junit.framework.TestCase;
import org.monsoon.framework.core.context.ApplicationContext;
import org.monsoon.framework.core.Monsoon;
import org.monsoon.framework.core.annotations.MonsoonApplication;

@MonsoonApplication
public class AppTest extends TestCase {

    public void testApp() throws Exception {
        String[] args = {};
        ApplicationContext context = Monsoon.run(AppTest.class, args);

        SampleModal sampleModal = (SampleModal) context.getBean("sampleModal");
        SampleModal sampleModal1 = (SampleModal) context.getBean("sampleModal");

        System.out.println(sampleModal);
        System.out.println(sampleModal1);
    }
}
