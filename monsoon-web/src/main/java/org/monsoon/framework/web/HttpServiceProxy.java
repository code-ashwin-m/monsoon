package org.monsoon.framework.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.monsoon.framework.web.annotations.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpServiceProxy {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T create(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(HttpService.class)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with @HttpService");
        }
        HttpService serviceAnno = clazz.getAnnotation(HttpService.class);
        String baseUrl = serviceAnno.baseUrl();

        //noinspection unchecked
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[] { clazz },
                (proxy, method, args) -> {
                    Map<String, String> headers = new HashMap<>();
                    String queryString = "";

                    Annotation[][] paramAnnotations = method.getParameterAnnotations();

                    if (args != null && args.length > 0) {
                        for (int i = 0; i < args.length; i++) {
                            for (Annotation annotation : paramAnnotations[i]) {
                                if (annotation instanceof QueryParam) {
                                    QueryParam qp = (QueryParam) annotation;
                                    queryString += (queryString.isEmpty() ? "?" : "&") + qp.value() + "=" + args[i];
                                }
                                if (annotation instanceof Header) {
                                    Header h = (Header) annotation;
                                    headers.put(h.value(), args[i].toString());
                                }
                            }
                        }
                    }

                    String urlStr = baseUrl;

                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);

                        if (requestMapping.method().toLowerCase().equals("get")){
                            urlStr += requestMapping.path() + queryString;
                            return sendRequest("GET", urlStr, null, headers, method);
                        } else if (requestMapping.method().toLowerCase().equals("post")) {
                            urlStr += requestMapping.path() + queryString;
                            String payload = (args != null && args.length > 0) ? objectMapper.writeValueAsString(args[0]) : null;
                            return sendRequest("POST", urlStr, payload, headers, method);
                        } else {
                            throw new UnsupportedOperationException("Unsupported HTTP method on " + method.getName());
                        }
                    }
                    return null;
                }
        );
    }

    private static Object sendRequest(String httpMethod, String urlStr, String payload, Map<String, String> headers, Method method) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(httpMethod);
        conn.setDoInput(true);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            conn.setRequestProperty(entry.getKey(), entry.getValue());
        }

        if ("POST".equals(httpMethod) && payload != null) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes());
            }
        }

        int status = conn.getResponseCode();
        Map<String, List<String>> respHeaders = conn.getHeaderFields();

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) response.append(line);
        }


        if (method.getReturnType() == ResponseEntity.class){
            Class<?> bodyType = getInnerClass(method);
            return new ResponseEntity<>(status, respHeaders, objectMapper.readValue(response.toString(), bodyType));
        } else {
            return objectMapper.readValue(response.toString(), method.getReturnType());
        }
    }

    private static Class<?> getInnerClass(Method method) {
        Class<?> bodyType = Object.class;
        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericReturnType;
            Type[] args = pt.getActualTypeArguments();
            if (args.length > 0 && args[0] instanceof Class<?>) {
                bodyType = (Class<?>) args[0];
            }
        }
        return bodyType;
    }
}
