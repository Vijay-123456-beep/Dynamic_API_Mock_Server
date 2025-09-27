package com.vijay.mockserver.mock;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.mockserver.user.User;
import com.vijay.mockserver.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/mock")
public class DynamicMockController {

    private final MockEndpointService service;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final TemplatingService templatingService;

    public DynamicMockController(MockEndpointService service, UserRepository userRepository,
            ObjectMapper objectMapper, TemplatingService templatingService) {
        this.service = service;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.templatingService = templatingService;
    }

    @RequestMapping(value = "/**", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleDynamicEndpoint(@RequestHeader(value = "X-Mock-User", required = false) String username,
            @RequestHeader(value = "X-Delay-Override", required = false) Long delayOverride,
            @RequestHeader(value = "X-Status-Override", required = false) Integer statusOverride,
            @RequestHeader(value = "X-HTTP-Method-Override", required = false) String methodOverride,
            HttpServletRequest request) throws Exception {

        String method = (methodOverride != null ? methodOverride : request.getMethod()).toUpperCase();
        String path = request.getRequestURI().substring(5); // remove /mock

        Optional<User> ownerOpt = resolveOwner(username);
        if (ownerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"No user found for the given username or authentication\"}");
        }
        User owner = ownerOpt.get();
        Optional<MockEndpoint> found = service.findBestMatchForRequest(owner, path, method, request);
        if (found.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"No mock configured for this route\"}");
        }
        MockEndpoint mock = found.get();

        int delay = (mock.getDelayMs() != null) ? mock.getDelayMs().intValue() : 0;
        if (delayOverride != null) {
            delay = delayOverride.intValue();
        }
        if (delay > 0) {
            Thread.sleep(delay);
        }

        int status = (mock.getStatusCode() != null) ? mock.getStatusCode().intValue() : 200;
        if (statusOverride != null) {
            status = statusOverride;
        }
        String responseBody = mock.getResponseJson();

        // Process template if it contains Handlebars syntax
        if (responseBody.contains("{{") && responseBody.contains("}}")) {
            responseBody = templatingService.processTemplateWithRequest(responseBody, request);
        }

        if (!isJson(responseBody)) {
            responseBody = objectMapper.writeValueAsString(objectMapper.readTree(new String(responseBody.getBytes(StandardCharsets.UTF_8))));
        }

        // Metrics recording removed for simplicity
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBody);
    }

    private boolean isJson(String s) {
        try {
            objectMapper.readTree(s);
            return true;
        } catch (com.fasterxml.jackson.core.JsonProcessingException | IllegalArgumentException e) {
            return false;
        }
    }

    private Optional<User> resolveOwner(String usernameHeader) {
        if (usernameHeader != null && !usernameHeader.isBlank()) {
            return userRepository.findByUsername(usernameHeader);
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;
        if (username == null) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username);
    }
}
