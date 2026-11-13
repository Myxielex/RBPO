package com.example.recycling.controller;

import com.example.recycling.model.CollectionPoint;
import com.example.recycling.service.CollectionPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@RequestMapping("/api/collection-points")
public class CollectionPointController {

    private final CollectionPointService service;

    @GetMapping
    public List<CollectionPoint> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionPoint> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CollectionPoint> create(
            @RequestBody CollectionPoint point) {

        return ResponseEntity.status(201)
                .body(service.create(point));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CollectionPoint> update(
            @PathVariable Long id,
            @RequestBody CollectionPoint point) {

        return service.update(id, point)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.delete(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}