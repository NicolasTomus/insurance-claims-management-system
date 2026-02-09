package com.insurance.backend.infrastructure.persistence.repository.broker;

import com.insurance.backend.domain.broker.Broker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrokerRepository extends JpaRepository<Broker, Long> {

    boolean existsByBrokerCode(String brokerCode);

    Optional<Broker> findByBrokerCode(String brokerCode);

    Page<Broker> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
