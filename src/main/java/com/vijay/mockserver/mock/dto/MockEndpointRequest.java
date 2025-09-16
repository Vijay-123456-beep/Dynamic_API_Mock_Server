package com.vijay.mockserver.mock.dto;

import jakarta.validation.constraints.*;

public class MockEndpointRequest {

    @NotBlank
    private String endpoint;

    @NotBlank
    private String method;

    @NotBlank
    private String responseJson;

    @Min(0)
    private Integer delayMs = 0;

    @Min(100)
    @Max(599)
    private Integer statusCode = 200;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getResponseJson() {
        return responseJson;
    }

    public void setResponseJson(String responseJson) {
        this.responseJson = responseJson;
    }

    public Integer getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(Integer delayMs) {
        this.delayMs = delayMs;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
}
