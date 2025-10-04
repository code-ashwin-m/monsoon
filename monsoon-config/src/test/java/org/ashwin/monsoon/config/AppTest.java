package org.ashwin.monsoon.config;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

    public AppTest() {}

    public void testConfiguration() {
        String value = Configuration.get("db.url");
        System.out.println(value);
    }
}
