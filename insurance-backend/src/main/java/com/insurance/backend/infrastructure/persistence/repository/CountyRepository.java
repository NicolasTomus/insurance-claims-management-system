package com.insurance.backend.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.insurance.backend.domain.geography.County;

import java.util.List;

public interface CountyRepository extends JpaRepository<County, Long> {
    List<County> findByCountryId(Long countryId);
}
