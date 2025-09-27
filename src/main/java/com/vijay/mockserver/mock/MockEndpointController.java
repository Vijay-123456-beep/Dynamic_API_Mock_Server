package com.vijay.mockserver.mock;

import com.vijay.mockserver.common.PaginationService;
import com.vijay.mockserver.mock.dto.MockEndpointRequest;
import com.vijay.mockserver.mock.dto.MockEndpointResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mock/endpoints")
public class MockEndpointController {

    private final MockEndpointService service;

    public MockEndpointController(MockEndpointService service) {
        this.service = service;
    }

    @GetMapping
    public List<MockEndpointResponse> list() {
        return service.listForCurrentUser();
    }

    @GetMapping("/paged")
    public PaginationService.PagedResponse<MockEndpointResponse> listPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return service.listForCurrentUserPaged(page, size, sortBy, sortDirection);
    }

    @GetMapping("/filtered")
    public PaginationService.PagedResponse<MockEndpointResponse> listFiltered(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String endpoint,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) Boolean isActive) {
        return service.listForCurrentUserWithFilters(page, size, sortBy, sortDirection, endpoint, method, isActive);
    }

    @GetMapping("/search")
    public PaginationService.PagedResponse<MockEndpointResponse> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam String q) {
        return service.searchForCurrentUser(page, size, sortBy, sortDirection, q);
    }

    @PostMapping
    public ResponseEntity<MockEndpointResponse> create(@Valid @RequestBody MockEndpointRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MockEndpointResponse> update(@PathVariable Long id, @RequestBody MockEndpointRequest request) {
        return ResponseEntity.ok(service.updateAsNewVersion(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
