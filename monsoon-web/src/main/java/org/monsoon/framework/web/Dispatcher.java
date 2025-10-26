package org.monsoon.framework.web;

import org.monsoon.framework.core.MonsoonApplication;
import org.monsoon.framework.web.annotations.*;
import org.monsoon.framework.web.autoconfigure.DefaultReqResHelper;
import org.monsoon.framework.web.interfaces.ReqResHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
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
    private static ReqResHelper reqResHelper;

    public Dispatcher(){
        reqResHelper = MonsoonApplication.getContext().getBeanOrNull("reqResHelper", ReqResHelper.class);
        if (reqResHelper == null){
            logger.error("Missing Jackson dependency, switching to default helper");
            reqResHelper = new DefaultReqResHelper();
        }
    }

    /**
     * Registers a controller with the dispatcher.
     * @param controller The controller to register.
     */
    public void registerController(Object controller) {
        Class<?> clazz = controller.getClass();
        if (!clazz.isAnnotationPresent(RestController.class)) return;
        logger.debug("Registering controller: {}", clazz.getSimpleName());
        for (Method method : clazz.getDeclaredMethods()){
            if (method.isAnnotationPresent(RequestMapping.class)){
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                String regex = pathToRegex(requestMapping.path());
                Pattern pattern = Pattern.compile(regex);
                routes.add(new Route(
                        requestMapping.method().toUpperCase(),
                        requestMapping.path(),
                        pattern,
                        controller,
                        method)
                );
                logger.debug("Mapped {} {} -> {}", requestMapping.method(), requestMapping.path(), method.getName());
            }
        }
        logger.debug("Registered controller: {}", clazz.getSimpleName());
    }


    /**
     * Converts a path to a regular expression.
     * It replaces all occurrences of '{variable_name}' with '([^/]+)' to create a regex pattern.
     * @param path The path to convert to a regex.
     * @return The regex pattern.
     */
    private String pathToRegex(String path) {
        return "^" + path.replaceAll("\\{[^/]+}", "([^/]+)") + "$";
    }

    /**
     * Dispatches an HTTP request to a controller.
     * It iterates over all the registered controllers and their methods and checks if the HTTP request matches the method and path of the controller.
     * If it does, it invokes the method and returns the result.
     * @param httpMethod The HTTP method of the request.
     * @param path The path of the request.
     * @param rawQuery The raw query string of the request.
     * @param bodyStream The input stream of the request body.
     * @return The result of the controller method.
     * @throws Exception If there is an error while dispatching the request.
     */
    public DispatchResult dispatch(String httpMethod, String path, String rawQuery, InputStream bodyStream) throws Exception{
        Map<String, String> pathVars = new HashMap<>();
        Route matched = null;

        for (Route route : routes){
            if (!route.httpMethod.equals(httpMethod)) continue;
            Matcher matcher = route.pattern.matcher(path);
            if (matcher.matches()) {
                matched = route;
                String[] paramNames = extractPathVariableNames(route.originalPath);
                for (int i = 0; i < paramNames.length; i++) {
                    pathVars.put(paramNames[i], matcher.group(i + 1));
                }
                break;
            }
        }

        if (matched == null){
            return new DispatchResult(404, "Not Found");
        }

        try {
            Object object = invokeControllerMethod(matched, pathVars, rawQuery, bodyStream);
            boolean isResponseBody = matched.method.isAnnotationPresent(ResponseBody.class);
            String result = serializeResponse(object);
            return new DispatchResult(200, result, isResponseBody);
        } catch (Exception ex){
            logger.error("Error while dispatching http request", ex);
            return new DispatchResult(500, "Internal Server Error" + ex.getMessage());
        }
    }

    /**
     * Invokes a controller method with the given parameters.
     * @param route The route that matched the HTTP request.
     * @param pathVars The path variables of the HTTP request.
     * @param rawQuery The raw query string of the HTTP request.
     * @param bodyStream The input stream of the HTTP request body.
     * @return The result of the controller method.
     * @throws Exception If there is an error while invoking the controller method.
     */
    private Object invokeControllerMethod(Route route, Map<String, String> pathVars, String rawQuery, InputStream bodyStream) throws Exception {
        Method method = route.method;
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        Map<String, String> queryParams = parseQuery(rawQuery);

        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            if (p.isAnnotationPresent(PathVariable.class)) {
                String name = p.getAnnotation(PathVariable.class).value();
                args[i] = convertToType(pathVars.get(name), p.getType());
            } else if (p.isAnnotationPresent(QueryParam.class)) {
                String name = p.getAnnotation(QueryParam.class).value();
                args[i] = convertToType(queryParams.get(name), p.getType());
            } else if (p.isAnnotationPresent(RequestBody.class)) {
                args[i] = reqResHelper.readValue(bodyStream, p.getType());
            } else {
                args[i] = null;
            }
        }
        return method.invoke(route.controller, args);
    }

    /**
     * Converts a string to an object of the given type.
     * @param s the string to convert
     * @param type the type to convert to
     * @return the converted object
     */

    private Object convertToType(String s, Class<?> type) {
        if (type == Integer.class || type == int.class ){
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
     * The names are extracted by matching the given path against the regular expression
     * "\\{([^/]+)}". The matched groups are added to a list and then converted to an array.
     * @param path the path from which to extract the variable names
     * @return an array of variable names
     */
    private String[] extractPathVariableNames(String path) {
        List<String> names = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\{([^/]+)}").matcher(path);
        while (matcher.find()) names.add(matcher.group(1));
        return names.toArray(new String[0]);
    }

    /**
     * Serializes the given object to a JSON string.
     * If the object is null, an empty string is returned.
     * If the object is already a string, it is returned as is.
     * Otherwise, the object is converted to a JSON string using the ObjectMapper.
     * @param result the object to serialize
     * @return the JSON string representation of the object
     */
    private String serializeResponse(Object result) throws Exception {
        if (result == null) return "";
        if (result instanceof String) return (String) result;
        return reqResHelper.writeValueAsString(result);
    }

    /**
     * Parses the given query string into a map of key-value pairs.
     * The query string is expected to be in the format "key1=value1&key2=value2&...".
     * If the query string is null or empty, an empty map is returned.
     * @param rawQuery the query string to parse
     * @return a map of key-value pairs
     */
    private Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> map = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) return map;
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
     * Decodes the given string using UTF-8 encoding.
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

    /**
     * Represents a route in the dispatcher.
     * A route is defined by its HTTP method, original path, pattern, controller, and method.
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

        public DispatchResult(int status, String body){
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
}