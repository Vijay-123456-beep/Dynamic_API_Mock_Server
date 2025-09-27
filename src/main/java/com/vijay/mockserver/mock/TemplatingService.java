package com.vijay.mockserver.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TemplatingService {

    private final ObjectMapper objectMapper;

    public TemplatingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Process a template string with context data
     */
    public String processTemplate(String template, Map<String, Object> context) {
        try {
            String result = template;

            // Replace {{random min max}}
            Pattern randomPattern = Pattern.compile("\\{\\{random\\s+(\\d+)\\s+(\\d+)\\}\\}");
            Matcher randomMatcher = randomPattern.matcher(result);
            while (randomMatcher.find()) {
                Random random = new Random();
                int min = Integer.parseInt(randomMatcher.group(1));
                int max = Integer.parseInt(randomMatcher.group(2));
                String replacement = String.valueOf(random.nextInt(max - min + 1) + min);
                result = result.replace(randomMatcher.group(), replacement);
            }

            // Replace {{uuid}}
            result = result.replaceAll("\\{\\{uuid\\}\\}", UUID.randomUUID().toString());

            // Replace {{timestamp}}
            result = result.replaceAll("\\{\\{timestamp\\}\\}", String.valueOf(System.currentTimeMillis()));

            // Replace {{date}}
            result = result.replaceAll("\\{\\{date\\}\\}", java.time.Instant.now().toString());

            // Replace {{randomString length}}
            Pattern randomStringPattern = Pattern.compile("\\{\\{randomString\\s+(\\d+)\\}\\}");
            Matcher randomStringMatcher = randomStringPattern.matcher(result);
            while (randomStringMatcher.find()) {
                int length = Integer.parseInt(randomStringMatcher.group(1));
                String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
                StringBuilder sb = new StringBuilder();
                Random random = new Random();
                for (int i = 0; i < length; i++) {
                    sb.append(chars.charAt(random.nextInt(chars.length())));
                }
                result = result.replace(randomStringMatcher.group(), sb.toString());
            }

            return result;
        } catch (Exception e) {
            // If templating fails, return the original template
            return template;
        }
    }

    /**
     * Process a template string with request context
     */
    public String processTemplateWithRequest(String template, jakarta.servlet.http.HttpServletRequest request) {
        Map<String, Object> context = buildRequestContext(request);
        return processTemplate(template, context);
    }

    private Map<String, Object> buildRequestContext(jakarta.servlet.http.HttpServletRequest request) {
        Map<String, Object> context = new HashMap<>();

        // Request information
        context.put("request", Map.of(
                "method", request.getMethod(),
                "uri", request.getRequestURI(),
                "path", request.getPathInfo() != null ? request.getPathInfo() : "",
                "query", request.getQueryString() != null ? request.getQueryString() : "",
                "remoteAddr", request.getRemoteAddr(),
                "userAgent", request.getHeader("User-Agent"),
                "contentType", request.getContentType()
        ));

        // Headers
        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(name
                -> headers.put(name, request.getHeader(name))
        );
        context.put("headers", headers);

        // Query parameters
        Map<String, String[]> queryParams = request.getParameterMap();
        Map<String, Object> params = new HashMap<>();
        queryParams.forEach((key, values) -> {
            if (values.length == 1) {
                params.put(key, values[0]);
            } else {
                params.put(key, values);
            }
        });
        context.put("query", params);

        // Add utility functions
        context.put("utils", Map.of(
                "timestamp", System.currentTimeMillis(),
                "date", java.time.Instant.now().toString(),
                "uuid", UUID.randomUUID().toString()
        ));

        return context;
    }
}
