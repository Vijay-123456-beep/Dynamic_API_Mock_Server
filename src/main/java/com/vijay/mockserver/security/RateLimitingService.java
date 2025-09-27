package com.vijay.mockserver.security;

import com.google.common.util.concurrent.RateLimiter;
import com.vijay.mockserver.user.User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();

    /**
     * Check if request is allowed for the given user
     */
    public boolean isAllowed(User user) {
        String key = "user:" + user.getId();
        RateLimiter rateLimiter = getRateLimiter(key, user.getRateLimitPerMinute());
        return rateLimiter.tryAcquire();
    }

    /**
     * Check if request is allowed for the given IP
     */
    public boolean isAllowed(String ipAddress) {
        String key = "ip:" + ipAddress;
        RateLimiter rateLimiter = getRateLimiter(key, 60); // Default 60 requests per minute for IP
        return rateLimiter.tryAcquire();
    }

    /**
     * Get remaining tokens for user (simplified)
     */
    public long getRemainingTokens(User user) {
        // Guava RateLimiter doesn't provide remaining tokens directly
        // Return a simple estimate based on rate
        return user.getRateLimitPerMinute();
    }

    /**
     * Get remaining tokens for IP (simplified)
     */
    public long getRemainingTokens(String ipAddress) {
        // Guava RateLimiter doesn't provide remaining tokens directly
        return 60; // Default rate
    }

    private RateLimiter getRateLimiter(String key, int requestsPerMinute) {
        return rateLimiters.computeIfAbsent(key, k -> {
            // Convert requests per minute to requests per second
            double permitsPerSecond = requestsPerMinute / 60.0;
            return RateLimiter.create(permitsPerSecond);
        });
    }

    /**
     * Reset rate limit for user (admin function)
     */
    public void resetRateLimit(User user) {
        String key = "user:" + user.getId();
        rateLimiters.remove(key);
    }

    /**
     * Reset rate limit for IP (admin function)
     */
    public void resetRateLimit(String ipAddress) {
        String key = "ip:" + ipAddress;
        rateLimiters.remove(key);
    }
}
