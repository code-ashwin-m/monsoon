package org.monsoon.test;

import org.monsoon.framework.core.Monsoon;
import org.monsoon.framework.core.configuration.MonsoonApplication;
import org.monsoon.framework.core.context.ApplicationContext;

@MonsoonApplication
public class App 
{
    public static void main( String[] args ) throws Exception {
        ApplicationContext context = Monsoon.run(App.class, args);
        System.out.println(context.getClasses());
    }
}
