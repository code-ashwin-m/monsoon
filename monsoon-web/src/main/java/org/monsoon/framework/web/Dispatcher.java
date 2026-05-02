package org.monsoon.framework.web;

import org.monsoon.framework.core.Monsoon;
import org.monsoon.framework.core.annotations.Controller;
import org.monsoon.framework.core.utils.ClassUtils;
import org.monsoon.framework.web.annotations.*;
import org.monsoon.framework.web.autoconfigure.DefaultHttpConverterAutoConfiguration;
import org.monsoon.framework.web.autoconfigure.DefaultViewRendererAutoConfiguration;
import org.monsoon.framework.web.interfaces.HandlerInterceptor;
import org.monsoon.framework.web.interfaces.HttpMessageConverter;
import org.monsoon.framework.web.interfaces.ViewRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dispatcher is a class that is used to dispatch HTTP requests to controllers.
 * It registers controllers and their methods and maps them to HTTP requests.
 */

public class Dispatcher {
    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);
    private final List<Route> routes = new ArrayList<>();
    private static HttpMessageConverter httpMessageConverter;
    private static ViewRenderer viewRenderer;
    private final List<HandlerInterceptor> interceptors = new ArrayList<>();
    private final List<FilterRegistration> filterRegistry = new ArrayList<>();

    /**
     * Initializes the dispatcher.
     * It sets up the HTTP message converter and view renderer.
     */
    public Dispatcher() {
        httpMessageConverter = Monsoon.getContext().getBeanOrNull("httpMessageConverter", HttpMessageConverter.class);
        if (httpMessageConverter == null
                || httpMessageConverter instanceof DefaultHttpConverterAutoConfiguration.DefaultHttpMessageConverter) {
            logger.error("Missing Jackson dependency, switching to default converter");
        }

        viewRenderer = Monsoon.getContext().getBeanOrNull("viewRenderer", ViewRenderer.class);
        if (viewRenderer == null || viewRenderer instanceof DefaultViewRendererAutoConfiguration.DefaultViewRenderer) {
            logger.error("Missing ViewRenderer dependency, switching to default renderer");
        }
    }

    /**
     * Registers a controller with the dispatcher.
     * 
     * @param controller The controller to register.
     */
    public void registerController(Object controller) {
        Class<?> clazz = controller.getClass();
        if (!ClassUtils.isAnnotationPresent(clazz, Controller.class))
            return;
        logger.debug("Registering controller: {}", clazz.getSimpleName());
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                String regex = pathToRegex(requestMapping.path());
                Pattern pattern = Pattern.compile(regex);
                routes.add(new Route(
                        requestMapping.method().toUpperCase(),
                        requestMapping.path(),
                        pattern,
                        controller,
                        method));
                logger.debug("Mapped {} {} -> {}", requestMapping.method(), requestMapping.path(), method.getName());
            }
        }
        logger.debug("Registered controller: {}", clazz.getSimpleName());
    }

    /**
     * Converts a path to a regular expression.
     * It replaces all occurrences of '{variable_name}' with '([^/]+)' to create a
     * regex pattern.
     * 
     * @param path The path to convert to a regex.
     * @return The regex pattern.
     */
    private String pathToRegex(String path) {
        return "^" + path.replaceAll("\\{[^/]+}", "([^/]+)") + "$";
    }

    /**
     * Dispatches an HTTP request to a controller.
     * It iterates over all the registered controllers and their methods and checks
     * if the HTTP request matches the method and path of the controller.
     * If it does, it invokes the method and returns the result.
     */

    public DispatchResult dispatch(HttpServletRequest req, HttpServletResponse resp) {
        String contextPath = req.getContextPath();
        String requestURI = req.getRequestURI();
        String pathAfterContext = requestURI.substring(contextPath.length());

        logger.debug("Server address: {}:{}, path: {}", req.getServerName(), req.getServerPort(), pathAfterContext);
        Map<String, String> pathVars = new HashMap<>();
        Route matched = null;

        if (pathAfterContext.equals("/")) {
            if (serveStaticResource("/index.html", resp, req)) {
                return new DispatchResult(200, "Static Resource Served");
            }
        }

        for (Route route : routes) {
            if (!route.httpMethod.equals(req.getMethod().toUpperCase()))
                continue;
            Matcher matcher = route.pattern.matcher(pathAfterContext);
            if (matcher.matches()) {
                matched = route;
                String[] paramNames = extractPathVariableNames(route.originalPath);
                for (int i = 0; i < paramNames.length; i++) {
                    pathVars.put(paramNames[i], matcher.group(i + 1));
                }
                break;
            }
        }

        // Check for static resources
        if (matched == null) {
            if (serveStaticResource(pathAfterContext, resp, req)) {
                return new DispatchResult(200, "Static Resource Served");
            }

            try {
                logger.error("No matching route found for {} {}", req.getMethod(), pathAfterContext);
                resp.setStatus(404);
                resp.getWriter().write("Not Found");
            } catch (Exception e) {
            }
            return new DispatchResult(404, "Not Found");
        }

        RequestHandler handler = createHandler(matched);
        boolean isResponseBody;
        MethodResponse methodResponse;
        try {

            for (HandlerInterceptor interceptor : interceptors) {
                if (!interceptor.preHandle(req, resp, handler)) {
                    return new DispatchResult(403, "Forbidden");
                }
            }

            methodResponse = invokeControllerMethod(matched, pathVars, req.getQueryString(), req.getInputStream());

            for (HandlerInterceptor interceptor : interceptors) {
                interceptor.postHandle(req, resp, handler);
            }
        } catch (Exception ex) {
            logger.error("Error while dispatching http request", ex);
            return new DispatchResult(500, "Internal Server Error" + ex.getMessage());
        } finally {
            for (HandlerInterceptor interceptor : interceptors) {
                try {
                    interceptor.afterCompletion(req, resp, handler);
                } catch (Exception ignored) {
                }
            }
        }

        isResponseBody = matched.method.getAnnotation(ResponseBody.class) != null
                || ClassUtils.isAnnotationPresent(matched.controller.getClass(), ResponseBody.class);

        try {
            String result = "";
            if (isResponseBody) {
                result = serializeResponse(methodResponse.getData());
                resp.setContentType("application/json; charset=UTF-8");
            } else {
                result = renderView(methodResponse);
                if (result == null)
                    result = methodResponse.getData().toString();
                resp.setContentType("text/html; charset=UTF-8");
            }
            resp.setStatus(200);
            resp.getWriter().write(result);
            return new DispatchResult(200, result, isResponseBody);
        } catch (Exception e) {
            logger.error("Error while serializing response", e);
            return new DispatchResult(500, "Internal Server Error" + e.getMessage());
        }
    }

    private RequestHandler createHandler(Route route) {
        return new RequestHandler(route.controller, route.method);
    }

    /**
     * Invokes a controller method with the given parameters.
     * 
     * @param route      The route that matched the HTTP request.
     * @param pathVars   The path variables of the HTTP request.
     * @param rawQuery   The raw query string of the HTTP request.
     * @param bodyStream The input stream of the HTTP request body.
     * @return The result of the controller method.
     * @throws Exception If there is an error while invoking the controller method.
     */
    private MethodResponse invokeControllerMethod(Route route, Map<String, String> pathVars, String rawQuery,
            InputStream bodyStream) throws Exception {
        Method method = route.method;
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        Map<String, String> queryParams = parseQuery(rawQuery);
        ModelMap model = new ModelMap();
        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            if (p.getType().isAssignableFrom(ModelMap.class)) {
                args[i] = model;
            } else if (p.isAnnotationPresent(PathVariable.class)) {
                String name = p.getAnnotation(PathVariable.class).value();
                args[i] = convertToType(pathVars.get(name), p.getType());
            } else if (p.isAnnotationPresent(QueryParam.class)) {
                String name = p.getAnnotation(QueryParam.class).value();
                args[i] = convertToType(queryParams.get(name), p.getType());
            } else if (p.isAnnotationPresent(RequestBody.class)) {
                args[i] = httpMessageConverter.readValue(bodyStream, p.getType());
            } else {
                args[i] = null;
            }
        }
        return new MethodResponse(method.invoke(route.controller, args), model);
    }

    /**
     * Converts a string to an object of the given type.
     * 
     * @param s    the string to convert
     * @param type the type to convert to
     * @return the converted object
     */

    private Object convertToType(String s, Class<?> type) {
        if (type == Integer.class || type == int.class) {
            return Integer.parseInt(s);
        } else if (type == Long.class || type == long.class) {
            return Long.parseLong(s);
        } else if (type == Double.class || type == double.class) {
            return Double.parseDouble(s);
        } else if (type == Float.class || type == float.class) {
            return Float.parseFloat(s);
        } else {
            return s;
        }
    }

    /**
     * Extracts the names of the path variables from the given path.
     * The names are extracted by matching the given path against the regular
     * expression
     * "\\{([^/]+)}". The matched groups are added to a list and then converted to
     * an array.
     * 
     * @param path the path from which to extract the variable names
     * @return an array of variable names
     */
    private String[] extractPathVariableNames(String path) {
        List<String> names = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\{([^/]+)}").matcher(path);
        while (matcher.find())
            names.add(matcher.group(1));
        return names.toArray(new String[0]);
    }

    /**
     * Serializes the given object to a JSON string.
     * If the object is null, an empty string is returned.
     * If the object is already a string, it is returned as is.
     * Otherwise, the object is converted to a JSON string using the ObjectMapper.
     * 
     * @param result the object to serialize
     * @return the JSON string representation of the object
     */
    private String serializeResponse(Object result) throws Exception {
        if (result == null)
            return "";
        if (result instanceof String)
            return (String) result;
        return httpMessageConverter.writeValueAsString(result);
    }

    private String renderView(MethodResponse methodResponse) {
        return viewRenderer.render(methodResponse.getData(), methodResponse.getModel().getAttributes());
    }

    /**
     * Parses the given query string into a map of key-value pairs.
     * The query string is expected to be in the format
     * "key1=value1&key2=value2&...".
     * If the query string is null or empty, an empty map is returned.
     * 
     * @param rawQuery the query string to parse
     * @return a map of key-value pairs
     */
    private Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> map = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty())
            return map;
        for (String pair : rawQuery.split("&")) {
            String[] parts = pair.split("=");
            if (parts.length > 0) {
                String key = decode(parts[0]);
                String value = parts.length > 1 ? decode(parts[1]) : "";
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * Serves a static resource from the classpath.
     * The resource is expected to be located in the "/static" directory in the
     * classpath.
     * 
     * @param path The path of the resource to serve.
     * @param resp The HttpServletResponse object.
     * @param req  The HttpServletRequest object.
     * @return True if the resource was found and served, false otherwise.
     */
    private boolean serveStaticResource(String path, HttpServletResponse resp, HttpServletRequest req) {
        String[] locations = { "/static", "/public", "/META-INF/static", "/META-INF/public" };
        InputStream is = null;
        for (String location : locations) {
            String resourcePath = location + path;
            is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                break;
            }
        }

        if (is == null) {
            return false;
        }

        logger.debug("Serving static resource: {}", path);

        try {
            String mimeType = req.getServletContext().getMimeType(path);
            if (mimeType == null) {
                mimeType = getMimeType(path);
            }
            resp.setContentType(mimeType);

            OutputStream os = resp.getOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
            is.close();
            return true;
        } catch (IOException e) {
            logger.error("Error serving static resource: {}", path, e);
            return false;
        }
    }

    /**
     * Gets the mime type of the given path.
     * 
     * @param path the path
     * @return the mime type
     */
    private String getMimeType(String path) {
        if (path == null)
            return "application/octet-stream";
        String lowercasePath = path.toLowerCase();
        if (lowercasePath.endsWith(".html") || lowercasePath.endsWith(".htm"))
            return "text/html; charset=UTF-8";
        if (lowercasePath.endsWith(".css"))
            return "text/css";
        if (lowercasePath.endsWith(".js"))
            return "application/javascript";
        if (lowercasePath.endsWith(".json"))
            return "application/json";
        if (lowercasePath.endsWith(".png"))
            return "image/png";
        if (lowercasePath.endsWith(".jpg") || lowercasePath.endsWith(".jpeg"))
            return "image/jpeg";
        if (lowercasePath.endsWith(".gif"))
            return "image/gif";
        if (lowercasePath.endsWith(".svg"))
            return "image/svg+xml";
        if (lowercasePath.endsWith(".ico"))
            return "image/x-icon";
        if (lowercasePath.endsWith(".woff"))
            return "application/font-woff";
        if (lowercasePath.endsWith(".woff2"))
            return "application/font-woff2";
        if (lowercasePath.endsWith(".ttf"))
            return "application/font-ttf";
        if (lowercasePath.endsWith(".eot"))
            return "application/vnd.ms-fontobject";
        if (lowercasePath.endsWith(".otf"))
            return "application/font-otf";
        if (lowercasePath.endsWith(".sfnt"))
            return "application/font-sfnt";
        if (lowercasePath.endsWith(".wasm"))
            return "application/wasm";
        if (lowercasePath.endsWith(".xml"))
            return "application/xml";
        if (lowercasePath.endsWith(".pdf"))
            return "application/pdf";
        if (lowercasePath.endsWith(".zip"))
            return "application/zip";
        if (lowercasePath.endsWith(".tar"))
            return "application/x-tar";
        if (lowercasePath.endsWith(".gz"))
            return "application/gzip";
        if (lowercasePath.endsWith(".rar"))
            return "application/x-rar-compressed";
        if (lowercasePath.endsWith(".7z"))
            return "application/x-7z-compressed";
        if (lowercasePath.endsWith(".bz2"))
            return "application/x-bzip2";
        if (lowercasePath.endsWith(".mp4"))
            return "video/mp4";
        if (lowercasePath.endsWith(".mp3"))
            return "audio/mpeg";
        if (lowercasePath.endsWith(".wav"))
            return "audio/wav";
        if (lowercasePath.endsWith(".ogg"))
            return "audio/ogg";
        if (lowercasePath.endsWith(".m4a"))
            return "audio/mp4";
        if (lowercasePath.endsWith(".webm"))
            return "video/webm";

        return "application/octet-stream";
    }

    /**
     * Decodes the given string using UTF-8 encoding.
     * 
     * @param s the string to decode
     * @return the decoded string
     */
    private String decode(String s) {
        try {
            return URLDecoder.decode(s, String.valueOf(StandardCharsets.UTF_8));
        } catch (UnsupportedEncodingException e) {
            logger.error("Error while parsing: {}", s, e);
            return s;
        }
    }

    public void registerInterceptor(HandlerInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    public void registerFilter(FilterRegistration filterRegistration) {
        filterRegistry.add(filterRegistration);
    }

    public List<FilterRegistration> getFilterRegistry() {
        return filterRegistry;
    }

    /**
     * Represents a route in the dispatcher.
     * A route is defined by its HTTP method, original path, pattern, controller,
     * and method.
     */
    private static class Route {
        final String httpMethod;
        final String originalPath;
        final Pattern pattern;
        final Object controller;
        final Method method;

        Route(String httpMethod, String originalPath, Pattern pattern, Object controller, Method method) {
            this.httpMethod = httpMethod;
            this.originalPath = originalPath;
            this.pattern = pattern;
            this.controller = controller;
            this.method = method;
        }
    }

    /**
     * Represents the result of a dispatch operation.
     * A dispatch result is defined by its status, body, and isJson.
     */
    public class DispatchResult {
        public final int status;
        public final String body;
        public final Boolean isResponseBody;
        public String contentType = null;

        public DispatchResult(int status, String body) {
            this.status = status;
            this.body = body;
            this.isResponseBody = false;
        }

        public DispatchResult(int status, String body, Boolean isResponseBody) {
            this.status = status;
            this.body = body;
            this.isResponseBody = isResponseBody;
        }
    }

    class MethodResponse {
        private Object data;
        private ModelMap model;

        public MethodResponse(Object data, ModelMap model) {
            this.data = data;
            this.model = model;
        }

        public Object getData() {
            return data;
        }

        public ModelMap getModel() {
            return model;
        }
    }
}