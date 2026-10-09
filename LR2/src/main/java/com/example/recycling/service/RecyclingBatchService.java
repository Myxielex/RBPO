package com.example.recycling.service;

import com.example.recycling.model.RecyclingBatch;
import com.example.recycling.repository.RecyclingBatchRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecyclingBatchService {

    private final RecyclingBatchRepository repository;

    public RecyclingBatchService(RecyclingBatchRepository repository) {
        this.repository = repository;
    }

    public List<RecyclingBatch> getAll() {
        return repository.findAll();
    }

    public Optional<RecyclingBatch> getById(Long id) {
        return repository.findById(id);
    }

    public RecyclingBatch create(RecyclingBatch batch) {
        return repository.save(batch);
    }

    public Optional<RecyclingBatch> update(Long id, RecyclingBatch newBatch) {
        return repository.findById(id).map(batch -> {
            batch.setUser(newBatch.getUser());
            batch.setPoint(newBatch.getPoint());
            batch.setRecyclingType(newBatch.getRecyclingType());
            batch.setMass(newBatch.getMass());
            batch.setCollectionDate(newBatch.getCollectionDate());
            batch.setStatus(newBatch.getStatus());

            return repository.save(batch);
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