package com.vijay.mockserver.mock;

import com.vijay.mockserver.mock.dto.MockEndpointRequest;
import com.vijay.mockserver.mock.dto.MockEndpointResponse;
import com.vijay.mockserver.user.User;
import com.vijay.mockserver.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MockEndpointService {

    private final MockEndpointRepository repository;
    private final UserRepository userRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    public MockEndpointService(MockEndpointRepository repository, UserRepository userRepository, org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public List<MockEndpointResponse> listForCurrentUser() {
        User user = currentUser();
        return repository.findByUser(user).stream().map(this::toDto).collect(Collectors.toList());
    }

    public MockEndpointResponse create(MockEndpointRequest request) {
        User user = currentUser();
        MockEndpoint entity = new MockEndpoint();
        entity.setUser(user);
        entity.setEndpoint(normalizeEndpoint(request.getEndpoint()));
        entity.setMethod(request.getMethod().toUpperCase());
        entity.setResponseJson(request.getResponseJson());
        entity.setDelayMs(Optional.ofNullable(request.getDelayMs()).orElse(0));
        entity.setStatusCode(Optional.ofNullable(request.getStatusCode()).orElse(200));
        int nextVersion = repository.findTopByUserAndEndpointAndMethodOrderByVersionDesc(user, entity.getEndpoint(), entity.getMethod())
                .map(MockEndpoint::getVersion)
                .map(v -> v + 1)
                .orElse(1);
        entity.setVersion(nextVersion);
        repository.save(entity);
        sendUpdateEvent(user.getUsername(), "created", entity);
        return toDto(entity);
    }

    public MockEndpointResponse updateAsNewVersion(Long id, MockEndpointRequest request) {
        User user = currentUser();
        MockEndpoint base = repository.findById(id)
                .filter(me -> me.getUser().getId().equals(user.getId()))
                .orElseThrow();
        String targetEndpoint = request.getEndpoint() != null ? normalizeEndpoint(request.getEndpoint()) : base.getEndpoint();
        String targetMethod = request.getMethod() != null ? request.getMethod().toUpperCase() : base.getMethod();

        int nextVersion = repository.findTopByUserAndEndpointAndMethodOrderByVersionDesc(user, targetEndpoint, targetMethod)
                .map(MockEndpoint::getVersion)
                .map(v -> v + 1)
                .orElse(1);

        MockEndpoint entity = new MockEndpoint();
        entity.setUser(user);
        entity.setEndpoint(targetEndpoint);
        entity.setMethod(targetMethod);
        entity.setResponseJson(request.getResponseJson() != null ? request.getResponseJson() : base.getResponseJson());
        entity.setDelayMs(request.getDelayMs() != null ? request.getDelayMs() : base.getDelayMs());
        entity.setStatusCode(request.getStatusCode() != null ? request.getStatusCode() : base.getStatusCode());
        entity.setVersion(nextVersion);
        repository.save(entity);
        sendUpdateEvent(user.getUsername(), "updated", entity);
        return toDto(entity);
    }

    public Optional<MockEndpoint> findLatestByUserAndPathAndMethod(User user, String endpoint, String method) {
        return repository.findTopByUserAndEndpointAndMethodOrderByVersionDesc(user, normalizeEndpoint(endpoint), method.toUpperCase());
    }

    public void delete(Long id) {
        User user = currentUser();
        repository.findById(id).filter(me -> me.getUser().getId().equals(user.getId()))
                .ifPresent(repository::delete);
        sendUpdateEvent(user.getUsername(), "deleted", null);
    }

    private MockEndpointResponse toDto(MockEndpoint entity) {
        MockEndpointResponse dto = new MockEndpointResponse();
        dto.setId(entity.getId());
        dto.setEndpoint(entity.getEndpoint());
        dto.setMethod(entity.getMethod());
        dto.setResponseJson(entity.getResponseJson());
        dto.setDelayMs(entity.getDelayMs());
        dto.setStatusCode(entity.getStatusCode());
        dto.setVersion(entity.getVersion());
        return dto;
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username).orElseThrow();
    }

    private String normalizeEndpoint(String p) {
        if (p == null || p.isBlank()) {
            return "/";
        }
        String normalized = p.startsWith("/") ? p : "/" + p;
        if (normalized.startsWith("/mock/")) {
            normalized = normalized.substring(5);
        }
        return normalized.replaceAll("//+", "/");
    }

    private void sendUpdateEvent(String username, String action, MockEndpoint entity) {
        try {
            var payload = new java.util.HashMap<String, Object>();
            payload.put("user", username);
            payload.put("action", action);
            if (entity != null) {
                payload.put("endpoint", entity.getEndpoint());
                payload.put("method", entity.getMethod());
                payload.put("version", entity.getVersion());
            }
            messagingTemplate.convertAndSend("/topic/mock-updates", payload);
        } catch (Exception ignored) {
        }
    }
}
