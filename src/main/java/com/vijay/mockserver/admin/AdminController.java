package com.vijay.mockserver.admin;

import com.vijay.mockserver.security.RateLimitingService;
import com.vijay.mockserver.security.RoleBasedAccessService;
import com.vijay.mockserver.user.User;
import com.vijay.mockserver.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final RoleBasedAccessService roleBasedAccessService;
    private final RateLimitingService rateLimitingService;

    public AdminController(UserRepository userRepository, RoleBasedAccessService roleBasedAccessService, RateLimitingService rateLimitingService) {
        this.userRepository = userRepository;
        this.roleBasedAccessService = roleBasedAccessService;
        this.rateLimitingService = rateLimitingService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        User currentUser = getCurrentUser();
        if (!roleBasedAccessService.canPerformAdminOperations(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<User> updateUserRole(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        User currentUser = getCurrentUser();
        if (!roleBasedAccessService.canManageUsers(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        String roleStr = request.get("role");
        try {
            User.Role newRole = User.Role.valueOf(roleStr.toUpperCase());
            user.setRole(newRole);
            userRepository.save(user);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/users/{userId}/rate-limit")
    public ResponseEntity<User> updateUserRateLimit(@PathVariable Long userId, @RequestBody Map<String, Integer> request) {
        User currentUser = getCurrentUser();
        if (!roleBasedAccessService.canPerformAdminOperations(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        Integer rateLimit = request.get("rateLimitPerMinute");
        if (rateLimit != null && rateLimit > 0) {
            user.setRateLimitPerMinute(rateLimit);
            userRepository.save(user);
            // Reset the user's current rate limit bucket
            rateLimitingService.resetRateLimit(user);
        }
        
        return ResponseEntity.ok(user);
    }

    @PostMapping("/rate-limit/reset/{userId}")
    public ResponseEntity<Map<String, String>> resetUserRateLimit(@PathVariable Long userId) {
        User currentUser = getCurrentUser();
        if (!roleBasedAccessService.canPerformAdminOperations(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        rateLimitingService.resetRateLimit(user);
        
        return ResponseEntity.ok(Map.of("message", "Rate limit reset for user: " + user.getUsername()));
    }

    @PostMapping("/rate-limit/reset/ip/{ipAddress}")
    public ResponseEntity<Map<String, String>> resetIpRateLimit(@PathVariable String ipAddress) {
        User currentUser = getCurrentUser();
        if (!roleBasedAccessService.canPerformAdminOperations(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        rateLimitingService.resetRateLimit(ipAddress);
        
        return ResponseEntity.ok(Map.of("message", "Rate limit reset for IP: " + ipAddress));
    }

    @GetMapping("/rate-limit/status/{userId}")
    public ResponseEntity<Map<String, Object>> getUserRateLimitStatus(@PathVariable Long userId) {
        User currentUser = getCurrentUser();
        if (!roleBasedAccessService.canPerformAdminOperations(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        long remainingTokens = rateLimitingService.getRemainingTokens(user);
        
        Map<String, Object> status = Map.of(
            "userId", user.getId(),
            "username", user.getUsername(),
            "rateLimitPerMinute", user.getRateLimitPerMinute(),
            "remainingTokens", remainingTokens
        );
        
        return ResponseEntity.ok(status);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username).orElseThrow();
    }
}
