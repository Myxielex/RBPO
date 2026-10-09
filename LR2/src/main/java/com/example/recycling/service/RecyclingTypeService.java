package com.example.recycling.service;

import com.example.recycling.model.RecyclingType;
import com.example.recycling.repository.RecyclingTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecyclingTypeService {

    private final RecyclingTypeRepository repository;

    public RecyclingTypeService(RecyclingTypeRepository repository) {
        this.repository = repository;
    }

    public List<RecyclingType> getAll() {
        return repository.findAll();
    }

    public Optional<RecyclingType> getById(Long id) {
        return repository.findById(id);
    }

    public RecyclingType create(RecyclingType recyclingType) {
        return repository.save(recyclingType);
    }

    public Optional<RecyclingType> update(Long id, RecyclingType newType) {
        return repository.findById(id).map(type -> {
            type.setName(newType.getName());
            type.setUnit(newType.getUnit());
            type.setPointsPerKg(newType.getPointsPerKg());

            return repository.save(type);
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