package com.example.recycling.service;

import com.example.recycling.model.RecyclingType;
import com.example.recycling.repository.CollectionPointRepository;
import com.example.recycling.repository.RecyclingBatchRepository;
import com.example.recycling.repository.RecyclingTypeRepository;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecyclingTypeService {

    private final RecyclingTypeRepository repository;
    private final RecyclingBatchRepository batchRepository;
    private final CollectionPointRepository pointRepository;

    @Transactional(readOnly = true)
    public List<RecyclingType> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<RecyclingType> getById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public RecyclingType create(RecyclingType recyclingType) {
        validateType(recyclingType);
        recyclingType.setName(recyclingType.getName().trim());
        recyclingType.setUnit(recyclingType.getUnit().trim());
        return repository.save(recyclingType);
    }

    @Transactional
    public Optional<RecyclingType> update(Long id, RecyclingType newType) {
        RecyclingType type = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Тип вторсырья не найден"));
        validateType(newType);

        // Do not change the tariff while accepted batches have active bonus entries.
        // Otherwise previously awarded points would no longer match the current tariff.
        if (Double.compare(type.getPointsPerKg(), newType.getPointsPerKg()) != 0
                && batchRepository.existsByRecyclingType_IdAndStatus(id, RecyclingBatchService.ACCEPTED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Нельзя менять тариф, пока есть принятые партии этого типа вторсырья");
        }

        type.setName(newType.getName().trim());
        type.setUnit(newType.getUnit().trim());
        type.setPointsPerKg(newType.getPointsPerKg());
        return Optional.of(repository.save(type));
    }

    @Transactional
    public boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        if (batchRepository.existsByRecyclingType_Id(id) || pointRepository.existsByAcceptedTypes_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Нельзя удалить тип вторсырья, пока он используется партиями или пунктами приёма");
        }
        repository.deleteById(id);
        return true;
    }

    private void validateType(RecyclingType type) {
        if (type == null || type.getName() == null || type.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите название типа вторсырья");
        }
        if (type.getUnit() == null || type.getUnit().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите единицу измерения");
        }
        if (!Double.isFinite(type.getPointsPerKg()) || type.getPointsPerKg() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Баллы за килограмм должны быть больше нуля");
        }
    }
}
