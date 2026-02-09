package com.insurance.backend.service;

import com.insurance.backend.domain.broker.Broker;
import com.insurance.backend.domain.broker.BrokerStatus;
import com.insurance.backend.infrastructure.persistence.repository.broker.BrokerRepository;
import com.insurance.backend.web.dto.broker.BrokerCreateRequest;
import com.insurance.backend.web.dto.broker.BrokerDetailsResponse;
import com.insurance.backend.web.dto.broker.BrokerUpdateRequest;
import com.insurance.backend.web.exception.ConflictException;
import com.insurance.backend.web.exception.NotFoundException;
import com.insurance.backend.web.mapper.BrokerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrokerService {

    private static final Logger log = LoggerFactory.getLogger(BrokerService.class);

    private final BrokerRepository brokerRepository;
    private final BrokerMapper brokerMapper;

    public BrokerService(BrokerRepository brokerRepository, BrokerMapper brokerMapper) {
        this.brokerRepository = brokerRepository;
        this.brokerMapper = brokerMapper;
    }

    @Transactional
    public BrokerDetailsResponse create(BrokerCreateRequest request) {
        String code = request.brokerCode().trim();

        if (brokerRepository.existsByBrokerCode(code)) {
            throw new ConflictException("Broker code already exists: " + code);
        }

        Broker saved = brokerRepository.save(brokerMapper.toEntity(request));
        log.info("Broker created: id={}, code={}", saved.getId(), saved.getBrokerCode());
        return brokerMapper.toDetails(saved);
    }

    @Transactional(readOnly = true)
    public BrokerDetailsResponse getById(Long brokerId) {
        Broker broker = brokerRepository.findById(brokerId)
                .orElseThrow(() -> new NotFoundException("Broker not found: " + brokerId));
        return brokerMapper.toDetails(broker);
    }

    @Transactional
    public BrokerDetailsResponse update(Long brokerId, BrokerUpdateRequest request) {
        Broker broker = brokerRepository.findById(brokerId)
                .orElseThrow(() -> new NotFoundException("Broker not found:  " + brokerId));

        brokerMapper.applyUpdate(broker, request);
        Broker saved = brokerRepository.save(broker);

        log.info("Broker updated: id={}", saved.getId());
        return brokerMapper.toDetails(saved);
    }

    @Transactional
    public BrokerDetailsResponse activate(Long brokerId) {
        Broker broker = brokerRepository.findById(brokerId)
                .orElseThrow(() -> new NotFoundException("Broker not found:  " + brokerId));
        broker.setStatus(BrokerStatus.ACTIVE);
        Broker saved = brokerRepository.save(broker);
        log.info("Broker activated: id={}", saved.getId());
        return brokerMapper.toDetails(saved);
    }

    @Transactional
    public BrokerDetailsResponse deactivate(Long brokerId) {
        Broker broker = brokerRepository.findById(brokerId)
                .orElseThrow(() -> new NotFoundException("Broker not found: " + brokerId));
        broker.setStatus(BrokerStatus.INACTIVE);
        Broker saved = brokerRepository.save(broker);
        log.info("Broker deactivated: id={}", saved.getId());
        return brokerMapper.toDetails(saved);
    }

    @Transactional(readOnly = true)
    public Page<BrokerDetailsResponse> search(String name, String brokerCode, Pageable pageable) {
        Page<Broker> page;

        if (brokerCode != null && !brokerCode.isBlank()) {
            Broker one = brokerRepository.findByBrokerCode(brokerCode.trim())
                    .orElseThrow(() -> new NotFoundException("Broker not found for code: " + brokerCode));
            page = new PageImpl<>(java.util.List.of(one), pageable, 1);
        } else if (name != null && !name.isBlank()) {
            page = brokerRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
        } else {
            page = brokerRepository.findAll(pageable);
        }

        return page.map(brokerMapper::toDetails);
    }
}
