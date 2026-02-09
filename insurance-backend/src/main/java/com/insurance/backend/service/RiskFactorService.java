package com.insurance.backend.service;

import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;
import com.insurance.backend.infrastructure.persistence.repository.metadata.risk.RiskFactorConfigurationRepository;
import com.insurance.backend.web.dto.metadata.risk.RiskFactorCreateRequest;
import com.insurance.backend.web.dto.metadata.risk.RiskFactorResponse;
import com.insurance.backend.web.dto.metadata.risk.RiskFactorUpdateRequest;
import com.insurance.backend.web.exception.NotFoundException;
import com.insurance.backend.web.mapper.MetadataMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RiskFactorService {

    private static final Logger log = LoggerFactory.getLogger(RiskFactorService.class);

    private final RiskFactorConfigurationRepository riskRepository;
    private final MetadataMapper metadataMapper;

    public RiskFactorService(RiskFactorConfigurationRepository riskRepository, MetadataMapper metadataMapper) {
        this.riskRepository = riskRepository;
        this.metadataMapper = metadataMapper;
    }

    @Transactional
    public RiskFactorResponse create(RiskFactorCreateRequest request) {
        RiskFactorConfiguration risk = metadataMapper.toEntity(request);
        RiskFactorConfiguration saved = riskRepository.save(risk);

        log.info("Risk factor created: id={}, level={}", saved.getId(), saved.getLevel());
        return metadataMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public RiskFactorResponse getById(Long riskId) {
        RiskFactorConfiguration risk = riskRepository.findById(riskId)
                .orElseThrow(() -> new NotFoundException("Risk factor not found: " + riskId));

        return metadataMapper.toResponse(risk);
    }

    @Transactional
    public RiskFactorResponse update(Long riskId, RiskFactorUpdateRequest request) {
        RiskFactorConfiguration risk = riskRepository.findById(riskId)
                .orElseThrow(() -> new NotFoundException("Risk factor not found: " + riskId));

        metadataMapper.applyUpdate(risk, request);

        RiskFactorConfiguration saved = riskRepository.save(risk);
        log.info("Risk factor updated: id={}", saved.getId());

        return metadataMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<RiskFactorResponse> list(Pageable pageable) {
        return riskRepository.findAll(pageable).map(metadataMapper::toResponse);
    }
}
