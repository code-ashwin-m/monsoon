package org.ashwin;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.ashwin.monsoon.core.ApplicationContext;
import org.ashwin.monsoon.core.annotations.ComponentScan;

/**
 * Unit test for simple App.
 */
@ComponentScan("org.ashwin")
public class AppTest extends TestCase {

    public AppTest() {}

    public void testApp() throws Exception {
        ApplicationContext context = new ApplicationContext(AppTest.class);
    }
}
