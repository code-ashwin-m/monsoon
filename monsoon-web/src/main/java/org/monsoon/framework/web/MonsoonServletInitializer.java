package org.monsoon.framework.web;

import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.monsoon.framework.core.Monsoon;
import org.monsoon.framework.core.annotations.ComponentScan;
import org.monsoon.framework.core.annotations.Configuration;
import org.monsoon.framework.core.annotations.EnableAutoConfiguration;
import org.monsoon.framework.core.annotations.MonsoonApplication;
import org.monsoon.framework.core.interfaces.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.HandlesTypes;
import java.util.EnumSet;
import java.util.Set;

/**
 * This class implements the ServletContainerInitializer interface and is used to initialize the web application.
 * It is annotated with @HandlesTypes to specify the types of classes that it can handle.
 */
@HandlesTypes({MonsoonApplication.class, Configuration.class, ComponentScan.class, EnableAutoConfiguration.class})
public class MonsoonServletInitializer implements ServletContainerInitializer {
    private static final Logger logger = LoggerFactory.getLogger(MonsoonServletInitializer.class);

    /**
     * This function is invoked when the web application is being initialized.
     * It iterates over the set of classes given and finds the first class that
     * is annotated with @MonsoonApplication. It then creates an application
     * context using Monsoon.run() and refreshes the context to get the ServletWebAdapter.
     * The ServletWebAdapter is then registered with the ServletContext.
     *
     * @param set the set of classes to search for @MonsoonApplication
     * @param servletContext the ServletContext that the ServletWebAdapter is registered with
     * @throws ServletException if an error occurs while initializing the web application
     */
    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) throws ServletException {
        if (set.isEmpty()) {
            logger.error("No Monsoon Application found");
            return;
        }

        Class<?> mainClass = null;

        while (set.iterator().hasNext()){
            Class<?> clazz = set.iterator().next();
            if (clazz.isAnnotationPresent(MonsoonApplication.class)) {
                mainClass = clazz;
                break;
            }
            mainClass = clazz;
        }


        try {
            ApplicationContext context = Monsoon.run(mainClass, null);
            ServletWebAdapter server = (ServletWebAdapter) context.refresh();

            for (FilterRegistration filterRegistration : server.getDispatcher().getFilterRegistry()) {
                Filter filter = filterRegistration.getFilter();
                String filterName = filterRegistration.getFilterClass().getSimpleName();

                javax.servlet.FilterRegistration.Dynamic filterReg =
                        servletContext.addFilter(filterName, filter);

                for (String pattern : filterRegistration.getPattern()) {
                    filterReg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, pattern);
                }
            }

            ServletRegistration.Dynamic servlet =
                    servletContext.addServlet("dispatcher", server);
            servlet.addMapping("/");
            servlet.setLoadOnStartup(1);
            logger.info("Monsoon servlet auto-registered!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
