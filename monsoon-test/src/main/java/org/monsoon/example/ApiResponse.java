package org.monsoon.example;

public class ApiResponse {
    private Object value;
    private String status;

    public ApiResponse() {
    }

    private ApiResponse(Object value, String status) {
        this.value = value;
        this.status = status;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static ApiResponse success(Object value) {
        return new ApiResponse(value, "success");
    }
}
