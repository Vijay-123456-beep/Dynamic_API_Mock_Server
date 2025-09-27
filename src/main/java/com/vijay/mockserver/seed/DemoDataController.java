package com.vijay.mockserver.seed;

import com.vijay.mockserver.mock.MockEndpointRepository;
import com.vijay.mockserver.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class DemoDataController {

    private final UserRepository userRepository;
    private final MockEndpointRepository mockEndpointRepository;
    private final DataSeeder dataSeeder;

    public DemoDataController(UserRepository userRepository, MockEndpointRepository mockEndpointRepository, DataSeeder dataSeeder) {
        this.userRepository = userRepository;
        this.mockEndpointRepository = mockEndpointRepository;
        this.dataSeeder = dataSeeder;
    }

    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seedDemoData() {
        try {
            // Check if user is admin
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.status(403).body(Map.of("error", "Authentication required"));
            }

            // Clear existing data
            mockEndpointRepository.deleteAll();
            userRepository.deleteAll();

            // Seed new data
            dataSeeder.run();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Demo data seeded successfully");
            response.put("usersCreated", userRepository.count());
            response.put("mocksCreated", mockEndpointRepository.count());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to seed demo data: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDemoStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalMocks", mockEndpointRepository.count());
        stats.put("activeMocks", mockEndpointRepository.countByIsActiveTrue());
        stats.put("mocksByUser", userRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                    user -> user.getUsername(),
                    user -> mockEndpointRepository.countByUser(user)
                )));
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetDemoData() {
        try {
            // Check if user is admin
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.status(403).body(Map.of("error", "Authentication required"));
            }

            // Clear all data
            mockEndpointRepository.deleteAll();
            userRepository.deleteAll();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "All demo data cleared");
            response.put("usersRemaining", userRepository.count());
            response.put("mocksRemaining", mockEndpointRepository.count());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to reset demo data: " + e.getMessage()));
        }
    }
}
