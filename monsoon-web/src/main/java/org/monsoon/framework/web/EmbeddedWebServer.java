package org.monsoon.framework.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.monsoon.framework.web.interfaces.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * EmbeddedWebServer is a server that can be embedded into a Java application.
 * It uses the com.sun.net.httpserver.HttpServer class to create an HTTP server.
 * The server can be started by calling the start method and stopped by calling the stop method.
 * The server can be configured to listen on a specific port and IP address.
 */
public class EmbeddedWebServer implements Server {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddedWebServer.class);
    private final Dispatcher dispatcher = new Dispatcher();
    private HttpServer server;

    /**
     * Starts the server on the specified port.
     *
     * @param port the port number on which the server should listen
     * @throws Exception if an error occurs while starting the server
     */
    @Override
    public void start(int port) throws Exception {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", exchange -> {
            try {
                handleRequest(exchange);
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        });
        server.start();
    }

    /**
     * Stops the server.
     */
    @Override
    public void stop() {
        server.stop(0);
    }

    @Override
    public void registerController(Object controller) {
        dispatcher.registerController(controller);
    }

    /**
     * Handles an HTTP request.
     *
     * @param exchange the HTTP exchange
     * @throws Exception if an error occurs while handling the request
     */
    private void handleRequest(HttpExchange exchange) throws Exception{
        try (InputStream body = exchange.getRequestBody()){
            Dispatcher.DispatchResult result = dispatcher.dispatch(
                    exchange.getRequestMethod(),
                    exchange.getRequestURI().getPath(),
                    exchange.getRequestURI().getRawQuery(),
                    body
            );
            byte[] bytes = result.body.getBytes();
            String contentType = result.isResponseBody ? "application/json; charset=UTF-8" : "text/plain; charset=UTF-8";
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(result.status, bytes.length);
            try(OutputStream os = exchange.getResponseBody()){
                os.write(bytes);
            }
            exchange.close();
        } catch (Exception ex) {
            logger.error("Error processing http request", ex);
            exchange.sendResponseHeaders(500, 0);
        }
    }
}
