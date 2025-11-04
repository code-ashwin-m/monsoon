package org.monsoon.framework.web;

import org.monsoon.framework.web.interfaces.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * This class extends HttpServlet and is used to handle HTTP requests.
 * It is used to dispatch requests to the appropriate controller.
 */
public class ServletWebAdapter extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ServletWebAdapter.class);
    private Dispatcher dispatcher;

    public ServletWebAdapter(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * Initializes the ServletWebAdapter.
     * This method is called when the ServletWebAdapter is initialized in the servlet container.
     * It logs a message indicating that the Monsoon framework has detected the servlet container.
     *
     * @param config the ServletConfig object
     * @throws ServletException if an error occurs while initializing the ServletWebAdapter
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        logger.info("Monsoon: Servlet container detected");
    }

    /**
     * Handles HTTP requests.
     * This method is called when a request is received by the servlet container.
     * It dispatches the request to the appropriate controller and returns the response.
     *
     * @param req the HttpServletRequest object
     * @param resp the HttpServletResponse object
     * @throws ServletException if an error occurs while handling the request
     * @throws IOException if an I/O error occurs while handling the request
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            handleController(req, resp);
        } catch (Exception ex) {
            ex.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"Internal Server Error\"}");
        }
    }

    private void handleController(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Dispatcher.DispatchResult result = dispatcher.dispatch(req,resp);

//        String contentType = result.isResponseBody ? "application/json; charset=UTF-8" : "text/plain; charset=UTF-8";
//        resp.setStatus(result.status);
//        resp.setContentType(contentType);
//        resp.getWriter().write(result.body);
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }
}
