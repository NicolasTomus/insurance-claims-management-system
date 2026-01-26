package com.insurance.backend.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.insurance.backend.domain.geography.Country;

public interface CountryRepository extends JpaRepository<Country, Long> { }
