package com.insurance.backend.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.insurance.backend.domain.geography.City;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByCountyId(Long countyId);
}
