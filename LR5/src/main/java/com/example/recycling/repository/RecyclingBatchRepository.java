package com.example.recycling.repository;

import com.example.recycling.model.RecyclingBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface RecyclingBatchRepository extends JpaRepository<RecyclingBatch, Long> {

    boolean existsByUser_Id(Long userId);

    List<RecyclingBatch> findByUser_Username(String username);

    java.util.Optional<RecyclingBatch> findByIdAndUser_Username(Long id, String username);

    boolean existsByPoint_Id(Long pointId);

    boolean existsByRecyclingType_Id(Long recyclingTypeId);

    boolean existsByRecyclingType_IdAndStatus(Long recyclingTypeId, String status);

    List<RecyclingBatch> findByPoint_IdAndCollectionDateAndStatusIn(
            Long pointId, LocalDate collectionDate, Collection<String> statuses);

    List<RecyclingBatch> findByPoint_IdAndStatusIn(Long pointId, Collection<String> statuses);
}
