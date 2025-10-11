package org.monsoon.example;

import org.monsoon.framework.core.Monsoon;
import org.monsoon.framework.core.annotations.MonsoonApplication;
import org.monsoon.framework.core.context.ApplicationContext;

@MonsoonApplication
public class Main {
    public static void main(String[] args) throws Exception {
        ApplicationContext context = Monsoon.run(Main.class);
        context.refresh();
    }
}
