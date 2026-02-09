package com.insurance.backend.infrastructure.persistence.repository.policy;

import com.insurance.backend.domain.policy.Policy;
import com.insurance.backend.domain.policy.PolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    @Query("""
            select p from Policy p
            where (:clientId is null or p.client.id = :clientId)
              and (:brokerId is null or p.broker.id = :brokerId)
              and (:status is null or p.status = :status)
              and (:startDate is null or p.endDate >= :startDate)
              and (:endDate is null or p.startDate <= :endDate)
            """)
    Page<Policy> search(Long clientId, Long brokerId, PolicyStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
