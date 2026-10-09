package com.example.recycling.repository;

import com.example.recycling.model.BonusEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BonusEntryRepository
        extends JpaRepository<BonusEntry, Long> {
}