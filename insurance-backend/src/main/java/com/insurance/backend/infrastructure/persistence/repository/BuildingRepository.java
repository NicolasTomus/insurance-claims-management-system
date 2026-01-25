package com.insurance.backend.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.insurance.backend.domain.building.Building;

public interface BuildingRepository extends JpaRepository<Building, Long> {
    Page<Building> findByOwnerId(Long clientId, Pageable pageable);
}
