package com.vijay.mockserver.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.mockserver.user.User;
import com.vijay.mockserver.user.UserRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class RateLimitingFilter implements Filter {

    private final RateLimitingService rateLimitingService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public RateLimitingFilter(RateLimitingService rateLimitingService, UserRepository userRepository, ObjectMapper objectMapper) {
        this.rateLimitingService = rateLimitingService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Skip rate limiting for certain paths
        String path = httpRequest.getRequestURI();
        if (shouldSkipRateLimiting(path)) {
            chain.doFilter(request, response);
            return;
        }

        boolean allowed = false;
        String clientId = getClientIdentifier(httpRequest);

        // Check user-based rate limiting first
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<User> userOpt = userRepository.findByUsername(auth.getName());
            if (userOpt.isPresent()) {
                allowed = rateLimitingService.isAllowed(userOpt.get());
            }
        }

        // If user-based rate limiting didn't apply or failed, check IP-based
        if (!allowed) {
            allowed = rateLimitingService.isAllowed(clientId);
        }

        if (!allowed) {
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Rate limit exceeded");
            errorResponse.put("message", "Too many requests. Please try again later.");
            errorResponse.put("retryAfter", 60); // seconds

            objectMapper.writeValue(httpResponse.getWriter(), errorResponse);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean shouldSkipRateLimiting(String path) {
        // Skip rate limiting for health checks, actuator endpoints, console, and static resources
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/health")
                || path.equals("/")
                || path.equals("/console")
                || path.startsWith("/console")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/");
    }

    private String getClientIdentifier(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
