package org.monsoon.framework.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.monsoon.framework.web.annotations.*;

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

public class Dispatcher {
    private final List<Route> routes = new ArrayList<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public void registerController(Object controller) {
        Class<?> clazz = controller.getClass();
        if (! clazz.isAnnotationPresent(RestController.class)) return;

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
                System.out.println("Mapped " + requestMapping.method() + " " + requestMapping.path() + " → "
                        + clazz.getSimpleName() + "." + method.getName());
            }
        }
    }

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
            Object result = invokeControllerMethod(matched, pathVars, rawQuery, bodyStream);
            Boolean isJson = !(result instanceof String);
            String json = serializeResponse(result);
            return new DispatchResult(200, json, isJson);
        } catch (Exception ex){
            ex.printStackTrace();
            return new DispatchResult(500, "Internal Server Error" + ex.getMessage());
        }
    }

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
            } else if (p.isAnnotationPresent(RequestParam.class)) {
                String name = p.getAnnotation(RequestParam.class).value();
                args[i] = convertToType(queryParams.get(name), p.getType());
            } else if (p.isAnnotationPresent(RequestBody.class)) {
                args[i] = objectMapper.readValue(bodyStream, p.getType());
            } else {
                args[i] = null;
            }
        }
        return method.invoke(route.controller, args);
    }

    private Object convertToType(String s, Class<?> type) {
        if (type == Integer.class || type == int.class ){
            return Integer.parseInt(s);
        } else {
            return s;
        }
    }

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

    private String decode(String s) {
        try {
            return URLDecoder.decode(s, String.valueOf(StandardCharsets.UTF_8));
        } catch (UnsupportedEncodingException e) {
            System.out.println("Error while parsing: " + s);
            return s;
        }
    }

    private String serializeResponse(Object result) throws JsonProcessingException {
        if (result == null) return "";
        if (result instanceof String) return (String) result;
        return objectMapper.writeValueAsString(result);
    }

    private String[] extractPathVariableNames(String path) {
        List<String> names = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\{([^/]+)}").matcher(path);
        while (matcher.find()) names.add(matcher.group(1));
        return names.toArray(new String[0]);
    }

    private String pathToRegex(String path) {
        return "^" + path.replaceAll("\\{[^/]+}", "([^/]+)") + "$";
    }

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

    public class DispatchResult {
        public final int status;
        public final String body;
        public final Boolean isJosn;

        public DispatchResult(int status, String body){
            this.status = status;
            this.body = body;
            this.isJosn = false;
        }
        public DispatchResult(int status, String body, Boolean isJson) {
            this.status = status;
            this.body = body;
            this.isJosn = isJson;
        }
    }
}
