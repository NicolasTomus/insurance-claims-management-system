package com.insurance.backend.service;

import com.insurance.backend.domain.metadata.fee.FeeConfiguration;
import com.insurance.backend.infrastructure.persistence.repository.metadata.fee.FeeConfigurationRepository;
import com.insurance.backend.web.dto.metadata.fee.FeeConfigurationCreateRequest;
import com.insurance.backend.web.dto.metadata.fee.FeeConfigurationResponse;
import com.insurance.backend.web.dto.metadata.fee.FeeConfigurationUpdateRequest;
import com.insurance.backend.web.exception.NotFoundException;
import com.insurance.backend.web.mapper.MetadataMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeeConfigurationService {

    private static final Logger log = LoggerFactory.getLogger(FeeConfigurationService.class);

    private final FeeConfigurationRepository feeRepository;
    private final MetadataMapper metadataMapper;

    public FeeConfigurationService(FeeConfigurationRepository feeRepository, MetadataMapper metadataMapper) {
        this.feeRepository = feeRepository;
        this.metadataMapper = metadataMapper;
    }

    @Transactional
    public FeeConfigurationResponse create(FeeConfigurationCreateRequest request) {
        FeeConfiguration fee = metadataMapper.toEntity(request);
        FeeConfiguration saved = feeRepository.save(fee);

        log.info("Fee configuration created: id={}, type={}", saved.getId(), saved.getType());
        return metadataMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public FeeConfigurationResponse getById(Long feeId) {
        FeeConfiguration fee = feeRepository.findById(feeId)
                .orElseThrow(() -> new NotFoundException("Fee configuration not found: " + feeId));

        return metadataMapper.toResponse(fee);
    }

    @Transactional
    public FeeConfigurationResponse update(Long feeId, FeeConfigurationUpdateRequest request) {
        FeeConfiguration fee = feeRepository.findById(feeId)
                .orElseThrow(() -> new NotFoundException("Fee configuration not found: " + feeId));

        metadataMapper.applyUpdate(fee, request);

        FeeConfiguration saved = feeRepository.save(fee);
        log.info("Fee configuration updated: id={}", saved.getId());

        return metadataMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<FeeConfigurationResponse> list(Pageable pageable) {
        return feeRepository.findAll(pageable).map(metadataMapper::toResponse);
    }
}
