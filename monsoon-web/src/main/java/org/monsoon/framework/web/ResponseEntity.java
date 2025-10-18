package org.monsoon.framework.web;

import java.util.List;
import java.util.Map;

public class ResponseEntity<T> {
    private int statusCode;
    private Map<String, List<String>> headers;
    private T body;

    public ResponseEntity(int statusCode, Map<String, List<String>> headers, T body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "ResponseEntity{" +
                "statusCode=" + statusCode +
                ", headers=" + headers +
                ", body=" + body +
                '}';
    }
}