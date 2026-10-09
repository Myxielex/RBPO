package com.example.recycling.controller;

import com.example.recycling.model.BonusEntry;
import com.example.recycling.service.BonusEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bonus-entries")
public class BonusEntryController {

    private final BonusEntryService service;

    public BonusEntryController(BonusEntryService service) {
        this.service = service;
    }

    @GetMapping
    public List<BonusEntry> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BonusEntry> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BonusEntry> create(
            @RequestBody BonusEntry entry) {

        return ResponseEntity.status(201)
                .body(service.create(entry));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BonusEntry> update(
            @PathVariable Long id,
            @RequestBody BonusEntry entry) {

        return service.update(id, entry)
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