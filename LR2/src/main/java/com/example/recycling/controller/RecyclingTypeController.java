package com.example.recycling.controller;

import com.example.recycling.model.RecyclingType;
import com.example.recycling.service.RecyclingTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recycling-types")
public class RecyclingTypeController {

    private final RecyclingTypeService service;

    public RecyclingTypeController(RecyclingTypeService service) {
        this.service = service;
    }

    @GetMapping
    public List<RecyclingType> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecyclingType> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RecyclingType> create(@RequestBody RecyclingType recyclingType) {
        return ResponseEntity.status(201)
                .body(service.create(recyclingType));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RecyclingType> update(
            @PathVariable Long id,
            @RequestBody RecyclingType recyclingType) {

        return service.update(id, recyclingType)
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