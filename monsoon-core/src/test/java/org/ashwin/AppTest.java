package org.ashwin;

import junit.framework.TestCase;
import org.ashwin.monsoon.core.ApplicationContext;
import org.ashwin.monsoon.core.annotations.ComponentScan;
import org.ashwin.test1.PrototypeTest;
import org.ashwin.test1.SingletonTest;

/**
 * Unit test for simple App.
 */
@ComponentScan("org.ashwin")
public class AppTest extends TestCase {

    public AppTest() {}

    public void testApp(){}
    public void testSingleton() throws Exception {
        ApplicationContext context = new ApplicationContext(AppTest.class);

        SingletonTest singletonTest1 = context.getBean(SingletonTest.class);
        SingletonTest singletonTest2 = context.getBean(SingletonTest.class);

        singletonTest1.setCount(100);
        singletonTest2.setCount(200);

        System.out.println("Singleton 1 : " + singletonTest1.getCount());
        System.out.println("Singleton 2 : " + singletonTest2.getCount());

        assertEquals(200, singletonTest1.getCount());
        assertEquals(200, singletonTest2.getCount());
    }

    public void testPrototype() throws Exception {
        ApplicationContext context = new ApplicationContext(AppTest.class);

        PrototypeTest prototypeTest1 = context.getBean(PrototypeTest.class);
        PrototypeTest prototypeTest2 = context.getBean(PrototypeTest.class);

        prototypeTest1.setCount(100);
        prototypeTest2.setCount(200);

        System.out.println("Prototype 1 : " + prototypeTest1.getCount());
        System.out.println("Prototype 2 : " + prototypeTest2.getCount());

        assertEquals(100, prototypeTest1.getCount());
        assertEquals(200, prototypeTest2.getCount());
    }
}
