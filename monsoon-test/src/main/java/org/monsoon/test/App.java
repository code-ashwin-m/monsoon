package org.monsoon.test;

import org.monsoon.framework.core.Monsoon;
import org.monsoon.framework.core.annotations.MonsoonApplication;
import org.monsoon.framework.core.context.ApplicationContext;

@MonsoonApplication
public class App 
{
    public static void main( String[] args ) throws Exception {
        ApplicationContext context = Monsoon.run(App.class, args);

        TestComponent component1 = context.getBean("testComponent", TestComponent.class);
        TestComponent component2 = context.getBean("testComponent", TestComponent.class);

        System.out.println(component1);
        System.out.println(component2);

        System.out.println(component1.getSingleton());
        System.out.println(component2.getSingleton());

        TestComponentSingleton singleton1 = context.getBean("testComponentSingleton", TestComponentSingleton.class);
        TestComponentSingleton singleton2 = context.getBean("testComponentSingleton", TestComponentSingleton.class);

        System.out.println(singleton1);
        System.out.println(singleton2);
    }
}
