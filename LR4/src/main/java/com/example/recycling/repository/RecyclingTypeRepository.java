package com.example.recycling.repository;

import com.example.recycling.model.RecyclingType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecyclingTypeRepository extends JpaRepository<RecyclingType, Long> {
}