package com.vijay.mockserver.mock;

import com.vijay.mockserver.common.PaginationService;
import com.vijay.mockserver.mock.dto.MockEndpointRequest;
import com.vijay.mockserver.mock.dto.MockEndpointResponse;
import com.vijay.mockserver.user.User;
import com.vijay.mockserver.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MockEndpointService {

    private final MockEndpointRepository repository;
    private final UserRepository userRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;
    private final RequestMatchingService requestMatchingService;

    public MockEndpointService(MockEndpointRepository repository, UserRepository userRepository, 
                             org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate,
                             RequestMatchingService requestMatchingService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.requestMatchingService = requestMatchingService;
    }

    public List<MockEndpointResponse> listForCurrentUser() {
        User user = currentUser();
        return repository.findByUser(user).stream().map(this::toDto).collect(Collectors.toList());
    }

    public PaginationService.PagedResponse<MockEndpointResponse> listForCurrentUserPaged(int page, int size, String sortBy, String sortDirection) {
        User user = currentUser();
        Pageable pageable = PaginationService.createPageable(page, size, sortBy, sortDirection);
        Page<MockEndpoint> pageResult = repository.findByUser(user, pageable);
        Page<MockEndpointResponse> responsePage = pageResult.map(this::toDto);
        return PaginationService.createPagedResponse(responsePage);
    }

    public PaginationService.PagedResponse<MockEndpointResponse> listForCurrentUserWithFilters(
            int page, int size, String sortBy, String sortDirection,
            String endpoint, String method, Boolean isActive) {
        User user = currentUser();
        Pageable pageable = PaginationService.createPageable(page, size, sortBy, sortDirection);
        Page<MockEndpoint> pageResult = repository.findByUserAndFilters(user, endpoint, method, isActive, pageable);
        Page<MockEndpointResponse> responsePage = pageResult.map(this::toDto);
        return PaginationService.createPagedResponse(responsePage);
    }

    public PaginationService.PagedResponse<MockEndpointResponse> searchForCurrentUser(
            int page, int size, String sortBy, String sortDirection, String searchTerm) {
        User user = currentUser();
        Pageable pageable = PaginationService.createPageable(page, size, sortBy, sortDirection);
        Page<MockEndpoint> pageResult = repository.findByUserAndSearchTerm(user, searchTerm, pageable);
        Page<MockEndpointResponse> responsePage = pageResult.map(this::toDto);
        return PaginationService.createPagedResponse(responsePage);
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
        entity.setRequestHeaders(request.getRequestHeaders());
        entity.setQueryParameters(request.getQueryParameters());
        entity.setRequestBodyPattern(request.getRequestBodyPattern());
        entity.setPriority(Optional.ofNullable(request.getPriority()).orElse(0));
        entity.setIsActive(Optional.ofNullable(request.getIsActive()).orElse(true));
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
        entity.setRequestHeaders(request.getRequestHeaders() != null ? request.getRequestHeaders() : base.getRequestHeaders());
        entity.setQueryParameters(request.getQueryParameters() != null ? request.getQueryParameters() : base.getQueryParameters());
        entity.setRequestBodyPattern(request.getRequestBodyPattern() != null ? request.getRequestBodyPattern() : base.getRequestBodyPattern());
        entity.setPriority(request.getPriority() != null ? request.getPriority() : base.getPriority());
        entity.setIsActive(request.getIsActive() != null ? request.getIsActive() : base.getIsActive());
        entity.setVersion(nextVersion);
        repository.save(entity);
        sendUpdateEvent(user.getUsername(), "updated", entity);
        return toDto(entity);
    }

    public Optional<MockEndpoint> findLatestByUserAndPathAndMethod(User user, String endpoint, String method) {
        return repository.findTopByUserAndEndpointAndMethodOrderByVersionDesc(user, normalizeEndpoint(endpoint), method.toUpperCase());
    }

    public Optional<MockEndpoint> findBestMatchForRequest(User user, String endpoint, String method, HttpServletRequest request) {
        List<MockEndpoint> candidates = repository.findByUserAndEndpointAndMethodOrderByPriorityDesc(user, normalizeEndpoint(endpoint), method.toUpperCase());
        return requestMatchingService.findBestMatch(candidates, request);
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
        dto.setRequestHeaders(entity.getRequestHeaders());
        dto.setQueryParameters(entity.getQueryParameters());
        dto.setRequestBodyPattern(entity.getRequestBodyPattern());
        dto.setPriority(entity.getPriority());
        dto.setIsActive(entity.getIsActive());
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
