package com.example.recycling.controller;

import com.example.recycling.dto.RegisterBatchRequest;
import com.example.recycling.dto.TransferPointRequest;
import com.example.recycling.model.RecyclingBatch;
import com.example.recycling.service.RecyclingBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recycling-batches")
public class RecyclingBatchController {

    private final RecyclingBatchService service;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public List<RecyclingBatch> getAll(Authentication authentication) {
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            return service.getAll();
        }
        return service.getAllForUser(authentication.getName());
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<RecyclingBatch> getById(
            @PathVariable Long id, Authentication authentication) {
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            return service.getById(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }
        return service.getByIdForUser(id, authentication.getName())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<RecyclingBatch> create(@RequestBody RecyclingBatch batch) {
        return ResponseEntity.status(201).body(service.create(batch));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<RecyclingBatch> update(
            @PathVariable Long id,
            @RequestBody RecyclingBatch batch) {
        return service.update(id, batch)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Business operation 1: register with domain checks.
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/business/register")
    public ResponseEntity<RecyclingBatch> register(
            @Valid @RequestBody RegisterBatchRequest request,
            Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return ResponseEntity.status(201).body(service.register(request));
        }
        return ResponseEntity.status(201)
                .body(service.registerForUser(request, authentication.getName()));
    }

    // Business operation 2: change batch state and create the bonus record atomically.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/confirm")
    public RecyclingBatch confirm(@PathVariable Long id) {
        return service.confirm(id);
    }

    // Business operation 3: reject a batch that is still pending.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/reject")
    public RecyclingBatch reject(@PathVariable Long id) {
        return service.reject(id);
    }

    // Business operation 4: transfer the pending batch to another valid point.
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/transfer")
    public RecyclingBatch transfer(
            @PathVariable Long id,
            @Valid @RequestBody TransferPointRequest request) {
        return service.transfer(id, request);
    }

    // Business operation 5: cancel an accepted batch and annul its bonus record atomically.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/cancel")
    public RecyclingBatch cancelAccepted(@PathVariable Long id) {
        return service.cancelAccepted(id);
    }
}
