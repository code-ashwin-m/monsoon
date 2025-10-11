package org.monsoon.framework.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.monsoon.framework.web.interfaces.WebServer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class EmbeddedWebServer implements WebServer {
    private final Dispatcher dispatcher = new Dispatcher();

    public void registerController(Object controller) {
        dispatcher.registerController(controller);
    }
    @Override
    public void start(int port) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", exchange -> {
            try {
                handleRequest(exchange);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        server.start();
        System.out.println("Server started at http://localhost:" + port);
    }

    private void handleRequest(HttpExchange exchange) throws Exception {
        try (InputStream body = exchange.getRequestBody()){
            Dispatcher.DispatchResult result = dispatcher.dispatch(
                    exchange.getRequestMethod(),
                    exchange.getRequestURI().getPath(),
                    exchange.getRequestURI().getRawQuery(),
                    body
            );
            byte[] bytes = result.body.getBytes();
            String contentType = result.isJosn ? "application/json; charset=UTF-8" : "text/plain; charset=UTF-8";
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(result.status, bytes.length);
            try(OutputStream os = exchange.getResponseBody()){
                os.write(bytes);
            }
            exchange.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            exchange.sendResponseHeaders(500, 0);
        }
    }
}
