package org.monsoon.framework.web;

import org.monsoon.framework.core.Monsoon;
import org.monsoon.framework.core.annotations.MonsoonApplication;
import org.monsoon.framework.core.context.ApplicationContext;
import org.monsoon.framework.web.interfaces.WebServer;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.HandlesTypes;

import java.util.Set;

@HandlesTypes(MonsoonApplication.class)
public class MonsoonServletInitializer implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) throws ServletException {
        if (set.isEmpty()) {
            System.out.println("No Monsoon Application found");
           return;
        }

        try {
            Class<?> mainClass = set.iterator().next();
            ApplicationContext context = Monsoon.run(mainClass);
            ServletWebAdapter server = (ServletWebAdapter) context.refresh();
            ServletRegistration.Dynamic servlet =
                    servletContext.addServlet("dispatcher", server);
            servlet.addMapping("/");
            servlet.setLoadOnStartup(1);
            System.out.println("Monsoon servlet auto-registered!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
