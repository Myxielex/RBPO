package com.example.recycling.repository;

import com.example.recycling.model.RecyclingBatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecyclingBatchRepository
        extends JpaRepository<RecyclingBatch, Long> {

    boolean existsByUser_Id(Long userId);

    boolean existsByPoint_Id(Long pointId);
}