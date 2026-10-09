package com.example.recycling.service;

import com.example.recycling.model.BonusEntry;
import com.example.recycling.model.RecyclingBatch;
import com.example.recycling.repository.BonusEntryRepository;
import com.example.recycling.repository.RecyclingBatchRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BonusEntryService {

    private final BonusEntryRepository repository;
    private final RecyclingBatchRepository batchRepository;

    public BonusEntryService(
            BonusEntryRepository repository,
            RecyclingBatchRepository batchRepository) {
        this.repository = repository;
        this.batchRepository = batchRepository;
    }

    public List<BonusEntry> getAll() {
        return repository.findAll();
    }

    public Optional<BonusEntry> getById(Long id) {
        return repository.findById(id);
    }

    public BonusEntry create(BonusEntry entry) {
        Long batchId = entry.getBatch().getId();

        RecyclingBatch batch = batchRepository.findById(batchId)
                .orElseThrow();

        entry.setBatch(batch);

        return repository.save(entry);
    }

    public Optional<BonusEntry> update(Long id, BonusEntry newEntry) {
        return repository.findById(id).map(entry -> {

            Long batchId = newEntry.getBatch().getId();

            RecyclingBatch batch = batchRepository.findById(batchId)
                    .orElseThrow();

            entry.setBatch(batch);
            entry.setPoints(newEntry.getPoints());
            entry.setStatus(newEntry.getStatus());

            return repository.save(entry);
        });
    }

    public boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }

        repository.deleteById(id);
        return true;
    }
}