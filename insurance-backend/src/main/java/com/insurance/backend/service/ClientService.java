package com.insurance.backend.service;

import com.insurance.backend.domain.client.Client;
import com.insurance.backend.infrastructure.persistence.repository.client.ClientRepository;
import com.insurance.backend.web.dto.client.ClientCreateRequest;
import com.insurance.backend.web.dto.client.ClientDetailsResponse;
import com.insurance.backend.web.dto.client.ClientSummaryResponse;
import com.insurance.backend.web.dto.client.ClientUpdateRequest;
import com.insurance.backend.web.exception.ConflictException;
import com.insurance.backend.web.exception.NotFoundException;
import com.insurance.backend.web.mapper.ClientMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public ClientService(ClientRepository clientRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
    }

    @Transactional
    public ClientDetailsResponse create(ClientCreateRequest request) {
        if (clientRepository.existsByIdentificationNumber(request.identificationNumber())) {
            throw new ConflictException("Identification number already exists: " + request.identificationNumber());
        }
        Client saved = clientRepository.save(clientMapper.toEntity(request));
        log.info("Client created: id={}, identifier={}", saved.getId(), saved.getIdentificationNumber());
        return clientMapper.toDetails(saved);
    }

    @Transactional(readOnly = true)
    public ClientDetailsResponse getById(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found: " + clientId));
        return clientMapper.toDetails(client);
    }

    @Transactional
    public ClientDetailsResponse update(Long clientId, ClientUpdateRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found: " + clientId));

        if (request.identificationNumber() != null && !Objects.equals(request.identificationNumber(), client.getIdentificationNumber())) {
            throw new ConflictException("Identification number changes are not allowed");
        }

        clientMapper.applyUpdate(client, request);
        Client saved = clientRepository.save(client);
        log.info("Client updated: id={}", saved.getId());
        return clientMapper.toDetails(saved);
    }

    @Transactional(readOnly = true)
    public Page<ClientSummaryResponse> search(String name, String identifier, Pageable pageable) {
        Page<Client> page;

        if (identifier != null && !identifier.isBlank()) {
            Client one = clientRepository.findByIdentificationNumber(identifier.trim())
                    .orElseThrow(() -> new NotFoundException("Client not found for identifier: " + identifier));
            page = new PageImpl<>(java.util.List.of(one), pageable, 1);
        } else if (name != null && !name.isBlank()) {
            page = clientRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
        } else {
            page = clientRepository.findAll(pageable);
        }

        return page.map(clientMapper::toSummary);
    }
}
