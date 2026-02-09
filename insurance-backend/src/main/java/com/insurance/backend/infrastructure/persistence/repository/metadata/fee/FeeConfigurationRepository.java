package com.insurance.backend.infrastructure.persistence.repository.metadata.fee;

import com.insurance.backend.domain.metadata.fee.FeeConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FeeConfigurationRepository extends JpaRepository<FeeConfiguration, Long> {

    List<FeeConfiguration> findAllByIdIn(List<Long> ids);

    @Query("""
            select f from FeeConfiguration f
            where f.active = true
              and (f.effectiveFrom is null or f.effectiveFrom <= :date)
              and (f.effectiveTo is null or f.effectiveTo >= :date)
            """)
    List<FeeConfiguration> findActiveForDate(@Param("date") LocalDate date);
}
