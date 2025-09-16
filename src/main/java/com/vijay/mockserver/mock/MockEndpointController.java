package com.vijay.mockserver.mock;

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
