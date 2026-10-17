package com.example.recycling.repository;

import com.example.recycling.model.BonusEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BonusEntryRepository extends JpaRepository<BonusEntry, Long> {

    boolean existsByBatch_Id(Long batchId);

    Optional<BonusEntry> findByBatch_Id(Long batchId);
}
