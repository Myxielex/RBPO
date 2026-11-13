package com.example.recycling.service;

import com.example.recycling.dto.RegisterBatchRequest;
import com.example.recycling.dto.TransferPointRequest;
import com.example.recycling.model.BonusEntry;
import com.example.recycling.model.CollectionPoint;
import com.example.recycling.model.RecyclingBatch;
import com.example.recycling.model.RecyclingType;
import com.example.recycling.model.User;
import com.example.recycling.repository.BonusEntryRepository;
import com.example.recycling.repository.CollectionPointRepository;
import com.example.recycling.repository.RecyclingBatchRepository;
import com.example.recycling.repository.RecyclingTypeRepository;
import com.example.recycling.repository.UserRepository;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RecyclingBatchService {

    public static final String PENDING = "Ожидает";
    public static final String ACCEPTED = "Принята";
    public static final String REJECTED = "Отклонена";
    public static final String CANCELLED = "Отменена";

    private static final List<String> ACTIVE_STATUSES = List.of(PENDING, ACCEPTED);

    private final RecyclingBatchRepository repository;
    private final UserRepository userRepository;
    private final CollectionPointRepository pointRepository;
    private final RecyclingTypeRepository typeRepository;
    private final BonusEntryRepository bonusRepository;

    @Transactional(readOnly = true)
    public List<RecyclingBatch> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<RecyclingBatch> getById(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<RecyclingBatch> getAllForUser(String username) {
        return repository.findByUser_Username(username);
    }

    @Transactional(readOnly = true)
    public Optional<RecyclingBatch> getByIdForUser(Long id, String username) {
        return repository.findByIdAndUser_Username(id, username);
    }

    // Business operation 1: a regular user can register a batch only for their own account.
    @Transactional
    public RecyclingBatch registerForUser(RegisterBatchRequest request, String username) {
        if (request == null) {
            throw badRequest("Не переданы данные сдачи");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> notFound("Пользователь не найден"));
        RecyclingBatch batch = prepareBatch(
                user.getId(),
                request.getPointId(),
                request.getRecyclingTypeId(),
                request.getMass(),
                request.getCollectionDate(),
                null);
        batch.setStatus(PENDING);
        return repository.save(batch);
    }

    // Business operation 1: register a batch after validating related objects.
    @Transactional
    public RecyclingBatch register(RegisterBatchRequest request) {
        if (request == null) {
            throw badRequest("Не переданы данные сдачи");
        }

        RecyclingBatch batch = prepareBatch(
                request.getUserId(),
                request.getPointId(),
                request.getRecyclingTypeId(),
                request.getMass(),
                request.getCollectionDate(),
                null);
        batch.setStatus(PENDING);
        return repository.save(batch);
    }

    // Keep the regular CRUD endpoint, but route it through the same checks.
    @Transactional
    public RecyclingBatch create(RecyclingBatch incoming) {
        if (incoming == null) {
            throw badRequest("Не переданы данные партии");
        }

        RecyclingBatch batch = prepareBatch(
                getId(incoming.getUser() == null ? null : incoming.getUser().getId(), "userId"),
                getId(incoming.getPoint() == null ? null : incoming.getPoint().getId(), "pointId"),
                getId(incoming.getRecyclingType() == null ? null : incoming.getRecyclingType().getId(), "recyclingTypeId"),
                incoming.getMass(),
                incoming.getCollectionDate(),
                null);
        batch.setStatus(PENDING);
        return repository.save(batch);
    }

    @Transactional
    public Optional<RecyclingBatch> update(Long id, RecyclingBatch incoming) {
        RecyclingBatch existing = repository.findById(id)
                .orElseThrow(() -> notFound("Партия не найдена"));
        requirePending(existing, "Изменять можно только ожидающую партию");

        if (incoming == null) {
            throw badRequest("Не переданы данные для изменения");
        }

        RecyclingBatch checked = prepareBatch(
                getId(incoming.getUser() == null ? null : incoming.getUser().getId(), "userId"),
                getId(incoming.getPoint() == null ? null : incoming.getPoint().getId(), "pointId"),
                getId(incoming.getRecyclingType() == null ? null : incoming.getRecyclingType().getId(), "recyclingTypeId"),
                incoming.getMass(),
                incoming.getCollectionDate(),
                id);

        existing.setUser(checked.getUser());
        existing.setPoint(checked.getPoint());
        existing.setRecyclingType(checked.getRecyclingType());
        existing.setMass(checked.getMass());
        existing.setCollectionDate(checked.getCollectionDate());
        // Status is controlled only by the business-operation endpoints.
        return Optional.of(repository.save(existing));
    }

    // Business operation 2: accept a batch and accrue its bonus in one transaction.
    @Transactional
    public RecyclingBatch confirm(Long id) {
        RecyclingBatch batch = findBatch(id);
        requirePending(batch, "Подтвердить можно только ожидающую партию");

        if (bonusRepository.existsByBatch_Id(id)) {
            throw conflict("Для этой партии уже существует начисление");
        }

        validatePoint(batch.getPoint(), batch.getRecyclingType());
        checkDailyLimit(batch.getPoint(), batch.getCollectionDate(), batch.getMass(), batch.getId());

        batch.setStatus(ACCEPTED);
        repository.save(batch);

        BonusEntry entry = new BonusEntry();
        entry.setBatch(batch);
        entry.setPoints(batch.getMass() * batch.getRecyclingType().getPointsPerKg());
        entry.setStatus("Начислено");
        bonusRepository.save(entry);

        return batch;
    }

    // Business operation 3: reject only a batch that has not been processed yet.
    @Transactional
    public RecyclingBatch reject(Long id) {
        RecyclingBatch batch = findBatch(id);
        requirePending(batch, "Отклонить можно только ожидающую партию");
        batch.setStatus(REJECTED);
        return repository.save(batch);
    }

    // Business operation 4: transfer a pending batch to another valid collection point.
    @Transactional
    public RecyclingBatch transfer(Long id, TransferPointRequest request) {
        RecyclingBatch batch = findBatch(id);
        requirePending(batch, "Перенести можно только ожидающую партию");

        if (request == null || request.getPointId() == null) {
            throw badRequest("Не указан новый пункт приёма");
        }

        CollectionPoint newPoint = pointRepository.findById(request.getPointId())
                .orElseThrow(() -> notFound("Новый пункт приёма не найден"));

        validatePoint(newPoint, batch.getRecyclingType());
        checkDailyLimit(newPoint, batch.getCollectionDate(), batch.getMass(),
                Objects.equals(newPoint.getId(), batch.getPoint().getId()) ? id : null);

        batch.setPoint(newPoint);
        return repository.save(batch);
    }

    // Business operation 5: cancel an accepted batch and reverse its bonus entry together.
    @Transactional
    public RecyclingBatch cancelAccepted(Long id) {
        RecyclingBatch batch = findBatch(id);
        if (!ACCEPTED.equals(batch.getStatus())) {
            throw conflict("Отменить этим способом можно только принятую партию");
        }

        BonusEntry entry = bonusRepository.findByBatch_Id(id)
                .orElseThrow(() -> conflict("У принятой партии отсутствует запись о начислении"));
        if (!"Начислено".equals(entry.getStatus())) {
            throw conflict("Баллы по этой партии уже аннулированы или не начислены");
        }

        entry.setStatus("Аннулировано");
        bonusRepository.save(entry);

        batch.setStatus(CANCELLED);
        return repository.save(batch);
    }

    @Transactional
    public boolean delete(Long id) {
        RecyclingBatch batch = repository.findById(id).orElse(null);
        if (batch == null) {
            return false;
        }
        if (bonusRepository.existsByBatch_Id(id)) {
            throw conflict("Нельзя удалить партию, у которой есть запись о начислении");
        }
        if (ACCEPTED.equals(batch.getStatus())) {
            throw conflict("Принятую партию нельзя удалить");
        }

        repository.delete(batch);
        return true;
    }

    private RecyclingBatch prepareBatch(
            Long userId,
            Long pointId,
            Long typeId,
            double mass,
            LocalDate collectionDate,
            Long excludedBatchId) {

        if (!Double.isFinite(mass) || mass <= 0) {
            throw badRequest("Масса должна быть больше нуля");
        }
        if (collectionDate == null) {
            throw badRequest("Укажите дату сдачи");
        }

        userId = getId(userId, "userId");
        pointId = getId(pointId, "pointId");
        typeId = getId(typeId, "recyclingTypeId");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> notFound("Пользователь не найден"));
        CollectionPoint point = pointRepository.findById(pointId)
                .orElseThrow(() -> notFound("Пункт приёма не найден"));
        RecyclingType type = typeRepository.findById(typeId)
                .orElseThrow(() -> notFound("Тип вторсырья не найден"));

        validatePoint(point, type);
        checkDailyLimit(point, collectionDate, mass, excludedBatchId);

        RecyclingBatch batch = new RecyclingBatch();
        batch.setUser(user);
        batch.setPoint(point);
        batch.setRecyclingType(type);
        batch.setMass(mass);
        batch.setCollectionDate(collectionDate);
        batch.setStatus(PENDING);
        return batch;
    }

    private void validatePoint(CollectionPoint point, RecyclingType type) {
        if (!point.isAvailability()) {
            throw conflict("Пункт приёма сейчас недоступен");
        }
        boolean acceptsType = point.getAcceptedTypes() != null
                && point.getAcceptedTypes().stream()
                .anyMatch(acceptedType -> acceptedType.getId().equals(type.getId()));
        if (!acceptsType) {
            throw conflict("Пункт приёма не принимает этот тип вторсырья");
        }
        if (!Double.isFinite(point.getDailyMassLimit()) || point.getDailyMassLimit() <= 0) {
            throw conflict("У пункта приёма задан некорректный дневной лимит");
        }
    }

    private void checkDailyLimit(
            CollectionPoint point, LocalDate date, double addedMass, Long excludedBatchId) {
        double currentMass = repository.findByPoint_IdAndCollectionDateAndStatusIn(
                        point.getId(), date, ACTIVE_STATUSES).stream()
                .filter(batch -> !Objects.equals(batch.getId(), excludedBatchId))
                .mapToDouble(RecyclingBatch::getMass)
                .sum();

        if (currentMass + addedMass > point.getDailyMassLimit()) {
            throw conflict("Превышен дневной лимит массы для пункта приёма");
        }
    }

    private RecyclingBatch findBatch(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> notFound("Партия не найдена"));
    }

    private void requirePending(RecyclingBatch batch, String message) {
        if (!PENDING.equals(batch.getStatus())) {
            throw conflict(message);
        }
    }

    private Long getId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw badRequest("Не указан корректный идентификатор: " + fieldName);
        }
        return id;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
