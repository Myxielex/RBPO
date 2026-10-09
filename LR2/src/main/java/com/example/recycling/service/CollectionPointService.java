package com.example.recycling.service;

import com.example.recycling.model.CollectionPoint;
import com.example.recycling.repository.CollectionPointRepository;
import org.springframework.stereotype.Service;
import com.example.recycling.repository.RecyclingBatchRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CollectionPointService {

    private final CollectionPointRepository repository;
    private final RecyclingBatchRepository batchRepository;

    public CollectionPointService(
            CollectionPointRepository repository,
            RecyclingBatchRepository batchRepository) {
        this.repository = repository;
        this.batchRepository = batchRepository;
    }

    public List<CollectionPoint> getAll() {
        return repository.findAll();
    }

    public Optional<CollectionPoint> getById(Long id) {
        return repository.findById(id);
    }

    public CollectionPoint create(CollectionPoint point) {
        return repository.save(point);
    }

    public Optional<CollectionPoint> update(
            Long id,
            CollectionPoint newPoint) {

        return repository.findById(id).map(point -> {
            point.setAddress(newPoint.getAddress());
            point.setAcceptedTypes(newPoint.getAcceptedTypes());
            point.setAvailability(newPoint.isAvailability());
            point.setDailyMassLimit(newPoint.getDailyMassLimit());

            return repository.save(point);
        });
    }

    public boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }

        if (batchRepository.existsByPoint_Id(id)) {
            return false;
        }

        repository.deleteById(id);
        return true;
    }
}