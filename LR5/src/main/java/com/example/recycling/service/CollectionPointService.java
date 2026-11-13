package com.example.recycling.service;

import com.example.recycling.model.CollectionPoint;
import com.example.recycling.model.RecyclingBatch;
import com.example.recycling.model.RecyclingType;
import com.example.recycling.repository.CollectionPointRepository;
import com.example.recycling.repository.RecyclingBatchRepository;
import com.example.recycling.repository.RecyclingTypeRepository;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class CollectionPointService {

    private final CollectionPointRepository repository;
    private final RecyclingBatchRepository batchRepository;
    private final RecyclingTypeRepository typeRepository;

    @Transactional(readOnly = true)
    public List<CollectionPoint> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<CollectionPoint> getById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public CollectionPoint create(CollectionPoint point) {
        validatePointFields(point);
        point.setAddress(point.getAddress().trim());
        point.setAcceptedTypes(loadTypes(point.getAcceptedTypes()));
        return repository.save(point);
    }

    @Transactional
    public Optional<CollectionPoint> update(Long id, CollectionPoint newPoint) {
        CollectionPoint point = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пункт приёма не найден"));
        validatePointFields(newPoint);

        List<RecyclingType> newTypes = loadTypes(newPoint.getAcceptedTypes());
        validateExistingBatches(point, newTypes, newPoint.getDailyMassLimit());

        point.setAddress(newPoint.getAddress().trim());
        point.setAcceptedTypes(newTypes);
        point.setAvailability(newPoint.isAvailability());
        point.setDailyMassLimit(newPoint.getDailyMassLimit());
        return Optional.of(repository.save(point));
    }

    @Transactional
    public boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        if (batchRepository.existsByPoint_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Нельзя удалить пункт приёма, к которому привязаны партии");
        }
        repository.deleteById(id);
        return true;
    }

    private void validateExistingBatches(
            CollectionPoint point, List<RecyclingType> newTypes, double newDailyLimit) {
        List<RecyclingBatch> activeBatches = batchRepository.findByPoint_IdAndStatusIn(
                point.getId(), List.of("Ожидает", "Принята"));

        // A pending batch must still be accepted by this point after its settings change.
        for (RecyclingBatch batch : activeBatches) {
            if ("Ожидает".equals(batch.getStatus())
                    && newTypes.stream().noneMatch(type -> type.getId().equals(batch.getRecyclingType().getId()))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Нельзя убрать тип материала, указанный в ожидающей партии");
            }
        }

        // Do not lower the daily limit below mass already registered for any date.
        Map<java.time.LocalDate, Double> massesByDate = new HashMap<>();
        for (RecyclingBatch batch : activeBatches) {
            massesByDate.merge(batch.getCollectionDate(), batch.getMass(), Double::sum);
        }
        for (double mass : massesByDate.values()) {
            if (mass > newDailyLimit) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Новый дневной лимит меньше массы уже зарегистрированных партий");
            }
        }
    }

    private void validatePointFields(CollectionPoint point) {
        if (point == null || point.getAddress() == null || point.getAddress().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите адрес пункта приёма");
        }
        if (!Double.isFinite(point.getDailyMassLimit()) || point.getDailyMassLimit() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дневной лимит должен быть больше нуля");
        }
        if (point.getAcceptedTypes() == null || point.getAcceptedTypes().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Укажите хотя бы один принимаемый тип вторсырья");
        }
    }

    private List<RecyclingType> loadTypes(List<RecyclingType> references) {
        List<RecyclingType> result = new ArrayList<>();
        for (RecyclingType reference : references) {
            if (reference == null || reference.getId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Для каждого принимаемого типа необходимо указать id");
            }
            RecyclingType type = typeRepository.findById(reference.getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Тип вторсырья не найден: " + reference.getId()));
            if (result.stream().noneMatch(loaded -> loaded.getId().equals(type.getId()))) {
                result.add(type);
            }
        }
        return result;
    }
}
