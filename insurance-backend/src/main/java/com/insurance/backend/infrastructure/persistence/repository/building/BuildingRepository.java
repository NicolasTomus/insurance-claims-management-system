package com.insurance.backend.infrastructure.persistence.repository.building;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.insurance.backend.domain.building.Building;

import java.util.Optional;

public interface BuildingRepository extends JpaRepository<Building, Long> {
    Page<Building> findByOwnerId(Long clientId, Pageable pageable);

    Optional<Building> findByIdAndOwnerId(Long id, Long ownerId);
}
