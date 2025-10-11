package org.monsoon.framework.web;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.monsoon.framework.core.context.ApplicationContext;
import org.monsoon.framework.web.interfaces.WebServer;

import java.io.IOException;

public class ServletWebAdapter extends HttpServlet implements WebServer {
    private final Dispatcher dispatcher = new Dispatcher();
    private ApplicationContext context;
    public ServletWebAdapter(ApplicationContext context) {
        this.context = context;
    }

    public void registerController(Object controller) {
        dispatcher.registerController(controller);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("Monsoon: Servlet container detected");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            String contextPath = req.getContextPath(); // returns "/monsoon-test-1.0"
            String requestURI = req.getRequestURI();   // returns "/monsoon-test-1.0/hello/ashwin"
            String pathAfterContext = requestURI.substring(contextPath.length());
            Dispatcher.DispatchResult result = dispatcher.dispatch(
                    req.getMethod(),
                    pathAfterContext,
                    req.getQueryString(),
                    req.getInputStream()
            );
            String contentType = result.isJosn ? "application/json; charset=UTF-8" : "text/plain; charset=UTF-8";
            resp.setStatus(result.status);
            resp.setContentType(contentType);
            resp.getWriter().write(result.body);
        } catch (Exception ex){
            ex.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"Internal Server Error\"}");
        }

    }

    @Override
    public void start(int port) throws Exception {
        System.out.println("Running in servlet container");
    }
}
