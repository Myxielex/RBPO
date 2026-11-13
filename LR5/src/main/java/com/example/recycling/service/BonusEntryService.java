package com.example.recycling.service;

import com.example.recycling.model.BonusEntry;
import com.example.recycling.model.RecyclingBatch;
import com.example.recycling.repository.BonusEntryRepository;
import com.example.recycling.repository.RecyclingBatchRepository;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BonusEntryService {

    private final BonusEntryRepository repository;
    private final RecyclingBatchRepository batchRepository;

    @Transactional(readOnly = true)
    public List<BonusEntry> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<BonusEntry> getById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public BonusEntry create(BonusEntry entry) {
        if (entry == null || entry.getBatch() == null || entry.getBatch().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите id партии для начисления");
        }

        RecyclingBatch batch = batchRepository.findById(entry.getBatch().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Партия не найдена"));
        if (!RecyclingBatchService.ACCEPTED.equals(batch.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Баллы можно начислять только за принятую партию");
        }
        if (repository.existsByBatch_Id(batch.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Для этой партии уже существует запись о начислении");
        }

        entry.setBatch(batch);
        entry.setPoints(batch.getMass() * batch.getRecyclingType().getPointsPerKg());
        entry.setStatus("Начислено");
        return repository.save(entry);
    }

    @Transactional
    public Optional<BonusEntry> update(Long id, BonusEntry newEntry) {
        BonusEntry entry = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Начисление не найдено"));

        if (newEntry == null || newEntry.getBatch() == null || newEntry.getBatch().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите id партии");
        }
        if (!entry.getBatch().getId().equals(newEntry.getBatch().getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Нельзя переносить запись о начислении на другую партию");
        }
        if (!"Начислено".equals(entry.getStatus())
                || !RecyclingBatchService.ACCEPTED.equals(entry.getBatch().getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Аннулированное начисление нельзя редактировать");
        }

        // Points are recalculated rather than accepted from the request body.
        entry.setPoints(entry.getBatch().getMass() * entry.getBatch().getRecyclingType().getPointsPerKg());
        return Optional.of(repository.save(entry));
    }

    @Transactional
    public boolean delete(Long id) {
        BonusEntry entry = repository.findById(id).orElse(null);
        if (entry == null) {
            return false;
        }
        if ("Начислено".equals(entry.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Нельзя удалить действующее начисление: используйте отмену партии");
        }
        repository.delete(entry);
        return true;
    }
}
