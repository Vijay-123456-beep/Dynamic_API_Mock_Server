package com.vijay.mockserver.seed;

import com.vijay.mockserver.mock.MockEndpoint;
import com.vijay.mockserver.mock.MockEndpointRepository;
import com.vijay.mockserver.user.User;
import com.vijay.mockserver.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MockEndpointRepository mockEndpointRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, MockEndpointRepository mockEndpointRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mockEndpointRepository = mockEndpointRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            seedUsers();
            seedMockEndpoints();
        }
    }

    private void seedUsers() {
        // Create demo users
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(User.Role.ADMIN);
        admin.setRateLimitPerMinute(1000);
        userRepository.save(admin);

        User developer = new User();
        developer.setUsername("developer");
        developer.setEmail("developer@example.com");
        developer.setPassword(passwordEncoder.encode("dev123"));
        developer.setRole(User.Role.USER);
        developer.setRateLimitPerMinute(200);
        userRepository.save(developer);

        User tester = new User();
        tester.setUsername("tester");
        tester.setEmail("tester@example.com");
        tester.setPassword(passwordEncoder.encode("test123"));
        tester.setRole(User.Role.USER);
        tester.setRateLimitPerMinute(100);
        userRepository.save(tester);
    }

    private void seedMockEndpoints() {
        User admin = userRepository.findByUsername("admin").orElseThrow();
        User developer = userRepository.findByUsername("developer").orElseThrow();
        User tester = userRepository.findByUsername("tester").orElseThrow();

        // Admin's mocks
        createMockEndpoint(admin, "/api/users", "GET", 200, 0, """
            {
              "users": [
                {"id": 1, "name": "John Doe", "email": "john@example.com", "role": "admin"},
                {"id": 2, "name": "Jane Smith", "email": "jane@example.com", "role": "user"}
              ],
              "total": 2,
              "page": 1
            }
            """, "{\"Authorization\": \"Bearer.*\"}", null, null, 10, true);

        createMockEndpoint(admin, "/api/users", "POST", 201, 100, """
            {
              "id": {{random 1 1000}},
              "name": "{{request.body.name}}",
              "email": "{{request.body.email}}",
              "createdAt": "{{date}}",
              "status": "created"
            }
            """, "{\"Content-Type\": \"application/json\"}", null, "{\"name\": \".+\", \"email\": \".+@.+\\\\..+\"}", 5, true);

        // Developer's mocks
        createMockEndpoint(developer, "/api/products", "GET", 200, 0, """
            {
              "products": [
                {"id": 1, "name": "Laptop", "price": 999.99, "category": "Electronics"},
                {"id": 2, "name": "Book", "price": 19.99, "category": "Education"}
              ],
              "total": 2
            }
            """, null, "{\"page\": \"\\\\d+\", \"limit\": \"\\\\d+\"}", null, 0, true);

        createMockEndpoint(developer, "/api/products", "POST", 201, 50, """
            {
              "id": {{random 1 10000}},
              "name": "{{request.body.name}}",
              "price": {{request.body.price}},
              "category": "{{request.body.category}}",
              "createdAt": "{{date}}",
              "sku": "SKU-{{randomString 8}}"
            }
            """, "{\"Content-Type\": \"application/json\"}", null, "{\"name\": \".+\", \"price\": \"\\\\d+\\\\.\\\\d+\"}", 0, true);

        createMockEndpoint(developer, "/api/orders", "GET", 200, 0, """
            {
              "orders": [
                {"id": 1, "userId": 1, "total": 1019.98, "status": "completed", "createdAt": "2024-01-15T10:30:00Z"},
                {"id": 2, "userId": 2, "total": 19.99, "status": "pending", "createdAt": "2024-01-15T11:45:00Z"}
              ],
              "total": 2
            }
            """, null, "{\"userId\": \"\\\\d+\"}", null, 0, true);

        // Tester's mocks
        createMockEndpoint(tester, "/api/auth/login", "POST", 200, 200, """
            {
              "token": "{{uuid}}",
              "user": {
                "id": {{random 1 100}},
                "username": "{{request.body.username}}",
                "role": "user"
              },
              "expiresIn": 3600,
              "timestamp": {{timestamp}}
            }
            """, "{\"Content-Type\": \"application/json\"}", null, "{\"username\": \".+\", \"password\": \".+\"}", 0, true);

        createMockEndpoint(tester, "/api/auth/register", "POST", 201, 300, """
            {
              "id": {{random 1 1000}},
              "username": "{{request.body.username}}",
              "email": "{{request.body.email}}",
              "createdAt": "{{date}}",
              "status": "active"
            }
            """, "{\"Content-Type\": \"application/json\"}", null, "{\"username\": \".+\", \"email\": \".+@.+\\\\..+\", \"password\": \".+\"}", 0, true);

        createMockEndpoint(tester, "/api/health", "GET", 200, 0, """
            {
              "status": "healthy",
              "timestamp": {{timestamp}},
              "version": "1.0.0",
              "uptime": "{{random 1000 86400}}s"
            }
            """, null, null, null, 0, true);

        // Error scenarios
        createMockEndpoint(tester, "/api/error/500", "GET", 500, 1000, """
            {
              "error": "Internal Server Error",
              "message": "Something went wrong",
              "timestamp": {{timestamp}},
              "requestId": "{{uuid}}"
            }
            """, null, null, null, 0, true);

        createMockEndpoint(tester, "/api/error/404", "GET", 404, 500, """
            {
              "error": "Not Found",
              "message": "Resource not found",
              "path": "{{request.uri}}",
              "timestamp": {{timestamp}}
            }
            """, null, null, null, 0, true);

        // Rate limiting test
        createMockEndpoint(tester, "/api/rate-limit", "GET", 200, 0, """
            {
              "message": "Rate limit test endpoint",
              "timestamp": {{timestamp}},
              "requestCount": {{random 1 100}}
            }
            """, null, null, null, 0, true);

        // Complex response with arrays
        createMockEndpoint(developer, "/api/analytics", "GET", 200, 0, """
            {
              "metrics": {
                "totalUsers": {{random 100 10000}},
                "activeUsers": {{random 50 5000}},
                "totalRequests": {{random 1000 100000}}
              },
              "charts": [
                {"name": "User Growth", "data": {{array 12 "{{random 100 1000}}"}}},
                {"name": "Request Volume", "data": {{array 24 "{{random 50 500}}"}}}
              ],
              "generatedAt": "{{date}}"
            }
            """, null, "{\"period\": \"day|week|month\"}", null, 0, true);
    }

    private void createMockEndpoint(User user, String endpoint, String method, int statusCode, int delayMs,
            String responseJson, String requestHeaders, String queryParameters,
            String requestBodyPattern, int priority, boolean isActive) {
        MockEndpoint mock = new MockEndpoint();
        mock.setUser(user);
        mock.setEndpoint(endpoint);
        mock.setMethod(method);
        mock.setStatusCode(statusCode);
        mock.setDelayMs(delayMs);
        mock.setResponseJson(responseJson);
        mock.setRequestHeaders(requestHeaders);
        mock.setQueryParameters(queryParameters);
        mock.setRequestBodyPattern(requestBodyPattern);
        mock.setPriority(priority);
        mock.setIsActive(isActive);
        mock.setVersion(1);
        mockEndpointRepository.save(mock);
    }
}
