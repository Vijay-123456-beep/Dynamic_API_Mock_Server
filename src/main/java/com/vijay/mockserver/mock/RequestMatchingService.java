package com.vijay.mockserver.mock;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class RequestMatchingService {

    private final ObjectMapper objectMapper;

    public RequestMatchingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Find the best matching mock endpoint based on request criteria
     */
    public Optional<MockEndpoint> findBestMatch(List<MockEndpoint> candidates, HttpServletRequest request) {
        return candidates.stream()
                .filter(MockEndpoint::getIsActive)
                .filter(mock -> matchesHeaders(mock, request))
                .filter(mock -> matchesQueryParameters(mock, request))
                .filter(mock -> matchesRequestBody(mock, request))
                .max((m1, m2) -> Integer.compare(m1.getPriority(), m2.getPriority()));
    }

    private boolean matchesHeaders(MockEndpoint mock, HttpServletRequest request) {
        if (mock.getRequestHeaders() == null || mock.getRequestHeaders().trim().isEmpty()) {
            return true; // No header constraints
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, String> expectedHeaders = objectMapper.readValue(
                    mock.getRequestHeaders(),
                    Map.class
            );

            for (Map.Entry<String, String> entry : expectedHeaders.entrySet()) {
                String headerName = entry.getKey();
                String expectedValue = entry.getValue();
                String actualValue = request.getHeader(headerName);

                if (actualValue == null || !matchesPattern(actualValue, expectedValue)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false; // Invalid JSON or other error
        }
    }

    private boolean matchesQueryParameters(MockEndpoint mock, HttpServletRequest request) {
        if (mock.getQueryParameters() == null || mock.getQueryParameters().trim().isEmpty()) {
            return true; // No query parameter constraints
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, String> expectedParams = objectMapper.readValue(
                    mock.getQueryParameters(),
                    Map.class
            );

            Map<String, String[]> actualParams = request.getParameterMap();

            for (Map.Entry<String, String> entry : expectedParams.entrySet()) {
                String paramName = entry.getKey();
                String expectedValue = entry.getValue();
                String[] actualValues = actualParams.get(paramName);

                if (actualValues == null || actualValues.length == 0) {
                    return false;
                }

                boolean foundMatch = false;
                for (String actualValue : actualValues) {
                    if (matchesPattern(actualValue, expectedValue)) {
                        foundMatch = true;
                        break;
                    }
                }
                if (!foundMatch) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false; // Invalid JSON or other error
        }
    }

    private boolean matchesRequestBody(MockEndpoint mock, HttpServletRequest request) {
        if (mock.getRequestBodyPattern() == null || mock.getRequestBodyPattern().trim().isEmpty()) {
            return true; // No body constraints
        }

        // For now, we'll implement basic JSON field matching
        // In a full implementation, you might want to support more complex patterns
        try {
            // Validate that the pattern is valid JSON
            objectMapper.readValue(mock.getRequestBodyPattern(), Map.class);

            // This is a simplified implementation
            // In practice, you'd need to read the request body and parse it
            // For now, we'll just return true if the pattern is valid JSON
            return true;
        } catch (Exception e) {
            return false; // Invalid JSON pattern
        }
    }

    private boolean matchesPattern(String actual, String pattern) {
        if (pattern.startsWith("regex:")) {
            // Regex pattern matching
            String regex = pattern.substring(6);
            return actual.matches(regex);
        } else if (pattern.startsWith("contains:")) {
            // Contains pattern matching
            String substring = pattern.substring(9);
            return actual.contains(substring);
        } else if (pattern.startsWith("startsWith:")) {
            // Starts with pattern matching
            String prefix = pattern.substring(11);
            return actual.startsWith(prefix);
        } else if (pattern.startsWith("endsWith:")) {
            // Ends with pattern matching
            String suffix = pattern.substring(9);
            return actual.endsWith(suffix);
        } else {
            // Exact match
            return actual.equals(pattern);
        }
    }
}
