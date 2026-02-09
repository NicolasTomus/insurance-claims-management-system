package com.insurance.backend.infrastructure.persistence.repository.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import com.insurance.backend.domain.client.Client;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsByIdentificationNumber(String identificationNumber);

    Optional<Client> findByIdentificationNumber(String identificationNumber);

    Page<Client> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
