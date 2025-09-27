package com.vijay.mockserver.mock;

import com.vijay.mockserver.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "mock_endpoints", indexes = {
    @Index(name = "idx_user_endpoint_method_version", columnList = "user_id,endpoint,method,version")
})
public class MockEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 255)
    private String endpoint; // e.g. /users, /products

    @Column(nullable = false, length = 10)
    private String method; // GET, POST, PUT, DELETE

    @Column(name = "response_json", columnDefinition = "TEXT", nullable = false)
    private String responseJson;

    @Column(name = "status_code", nullable = false)
    private Integer statusCode = 200;

    @Column(name = "delay_ms", nullable = false)
    private Integer delayMs = 0;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "request_headers", columnDefinition = "TEXT")
    private String requestHeaders; // JSON string for header matching rules

    @Column(name = "query_parameters", columnDefinition = "TEXT")
    private String queryParameters; // JSON string for query parameter matching rules

    @Column(name = "request_body_pattern", columnDefinition = "TEXT")
    private String requestBodyPattern; // JSON string for body matching rules

    @Column(name = "priority", nullable = false)
    private Integer priority = 0; // Higher priority mocks are matched first

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // Enable/disable mock

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

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

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Integer getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(Integer delayMs) {
        this.delayMs = delayMs;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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
