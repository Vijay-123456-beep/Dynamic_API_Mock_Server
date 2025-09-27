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

    private String requestHeaders; // JSON string for header matching rules
    private String queryParameters; // JSON string for query parameter matching rules
    private String requestBodyPattern; // JSON string for body matching rules
    private Integer priority = 0; // Higher priority mocks are matched first
    private Boolean isActive = true; // Enable/disable mock

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

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(String requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(String queryParameters) {
        this.queryParameters = queryParameters;
    }

    public String getRequestBodyPattern() {
        return requestBodyPattern;
    }

    public void setRequestBodyPattern(String requestBodyPattern) {
        this.requestBodyPattern = requestBodyPattern;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
