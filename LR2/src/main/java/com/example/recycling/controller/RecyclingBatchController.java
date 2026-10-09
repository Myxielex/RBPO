package com.example.recycling.controller;

import com.example.recycling.model.RecyclingBatch;
import com.example.recycling.service.RecyclingBatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recycling-batches")
public class RecyclingBatchController {

    private final RecyclingBatchService service;

    public RecyclingBatchController(RecyclingBatchService service) {
        this.service = service;
    }

    @GetMapping
    public List<RecyclingBatch> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecyclingBatch> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RecyclingBatch> create(
            @RequestBody RecyclingBatch batch) {

        return ResponseEntity.status(201)
                .body(service.create(batch));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RecyclingBatch> update(
            @PathVariable Long id,
            @RequestBody RecyclingBatch batch) {

        return service.update(id, batch)
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