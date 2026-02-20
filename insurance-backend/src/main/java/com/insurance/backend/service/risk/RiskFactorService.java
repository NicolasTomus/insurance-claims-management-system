package com.insurance.backend.service.risk;

import com.insurance.backend.domain.building.BuildingType;
import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;
import com.insurance.backend.domain.metadata.risk.RiskLevel;
import com.insurance.backend.infrastructure.persistence.repository.geografy.*;
import com.insurance.backend.infrastructure.persistence.repository.metadata.risk.RiskFactorConfigurationRepository;
import com.insurance.backend.web.dto.metadata.risk.RiskFactorCreateRequest;
import com.insurance.backend.web.dto.metadata.risk.RiskFactorResponse;
import com.insurance.backend.web.dto.metadata.risk.RiskFactorUpdateRequest;
import com.insurance.backend.web.exception.BadRequestException;
import com.insurance.backend.web.exception.ConflictException;
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
    private final CountryRepository countryRepository;
    private final CountyRepository countyRepository;
    private final CityRepository cityRepository;

    public RiskFactorService(
            RiskFactorConfigurationRepository riskRepository,
            MetadataMapper metadataMapper,
            CountryRepository countryRepository,
            CountyRepository countyRepository,
            CityRepository cityRepository
    ) {
        this.riskRepository = riskRepository;
        this.metadataMapper = metadataMapper;
        this.countryRepository = countryRepository;
        this.countyRepository = countyRepository;
        this.cityRepository = cityRepository;
    }

    @Transactional
    public RiskFactorResponse create(RiskFactorCreateRequest request) {
        validateCreate(request);

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

    private void validateCreate(RiskFactorCreateRequest request) {
        if (request.referenceId() == null) {
            throw new BadRequestException("referenceId is required");
        }

        if (request.level() == RiskLevel.BUILDING_TYPE) {
            long id = request.referenceId();
            long max = BuildingType.values().length - 1L;

            if (0 > id || id > max) {
                throw new BadRequestException("Unknown building type id: " + id + " (allowed: 1=Residential, 2=Office, 3=Industrial)");
            }
        }else {
            validateGeoReferenceExists(request.level(), request.referenceId());
        }

        if (request.active()) {
            boolean existsActive = !riskRepository
                    .findByLevelAndReferenceIdAndActiveTrue(request.level(), request.referenceId())
                    .isEmpty();

            if (existsActive) {
                throw new ConflictException("Active risk factor already exists for this " + request.level());
            }
        }
    }

    private void validateGeoReferenceExists(RiskLevel level, Long referenceId) {
        if (referenceId == null) {
            throw new BadRequestException("referenceId is required for level " + level);
        }

        boolean exists;
        switch (level) {
            case COUNTRY -> exists = countryRepository.existsById(referenceId);
            case COUNTY  -> exists = countyRepository.existsById(referenceId);
            case CITY    -> exists = cityRepository.existsById(referenceId);
            default      -> { return; }
        }

        if (!exists) {
            throw new BadRequestException(level + " not found: " + referenceId);
        }
    }

}

