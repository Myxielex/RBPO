package com.example.recycling.repository;

import com.example.recycling.model.CollectionPoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionPointRepository extends JpaRepository<CollectionPoint, Long> {

    boolean existsByAcceptedTypes_Id(Long recyclingTypeId);
}
