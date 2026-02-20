package com.insurance.backend.service;

import com.insurance.backend.domain.building.BuildingType;
import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;
import com.insurance.backend.domain.metadata.risk.RiskLevel;
import com.insurance.backend.infrastructure.persistence.repository.geografy.CityRepository;
import com.insurance.backend.infrastructure.persistence.repository.geografy.CountyRepository;
import com.insurance.backend.infrastructure.persistence.repository.geografy.CountryRepository;
import com.insurance.backend.infrastructure.persistence.repository.metadata.risk.RiskFactorConfigurationRepository;
import com.insurance.backend.service.risk.RiskFactorService;
import com.insurance.backend.web.dto.metadata.risk.RiskFactorCreateRequest;
import com.insurance.backend.web.dto.metadata.risk.RiskFactorResponse;
import com.insurance.backend.web.dto.metadata.risk.RiskFactorUpdateRequest;
import com.insurance.backend.web.exception.BadRequestException;
import com.insurance.backend.web.exception.ConflictException;
import com.insurance.backend.web.exception.NotFoundException;
import com.insurance.backend.web.mapper.MetadataMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskFactorServiceTest {

    private static final long RISK_ID = 101L;

    @Mock private RiskFactorConfigurationRepository riskRepository;
    @Mock private MetadataMapper metadataMapper;
    @Mock private CountryRepository countryRepository;
    @Mock private CountyRepository countyRepository;
    @Mock private CityRepository cityRepository;

    @InjectMocks private RiskFactorService riskFactorService;

    @Test
    void createShouldThrowBadRequestWhenReferenceIdNull() {
        RiskFactorCreateRequest req = new RiskFactorCreateRequest(
                RiskLevel.COUNTRY, null, new BigDecimal("0.10"), true
        );

        assertThrows(BadRequestException.class, () -> riskFactorService.create(req));

        verifyNoInteractions(riskRepository, metadataMapper, countryRepository, countyRepository, cityRepository);
    }

    @Test
    void createShouldThrowBadRequestWhenUnknownBuildingTypeId() {
        long invalidId = BuildingType.values().length;
        RiskFactorCreateRequest req = new RiskFactorCreateRequest(
                RiskLevel.BUILDING_TYPE, invalidId, new BigDecimal("0.10"), false
        );

        assertThrows(BadRequestException.class, () -> riskFactorService.create(req));

        verifyNoInteractions(riskRepository, metadataMapper, countryRepository, countyRepository, cityRepository);
    }

    @Test
    void createShouldThrowBadRequestWhenGeoReferenceDoesNotExist() {
        long refId = 77L;
        RiskFactorCreateRequest req = new RiskFactorCreateRequest(
                RiskLevel.CITY, refId, new BigDecimal("0.10"), false
        );

        when(cityRepository.existsById(refId)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> riskFactorService.create(req));

        verify(cityRepository).existsById(refId);
        verifyNoMoreInteractions(cityRepository);
        verifyNoInteractions(riskRepository, metadataMapper, countryRepository, countyRepository);
    }

    @Test
    void createShouldThrowConflictWhenActiveAlreadyExistsForLevelAndReference() {
        long refId = 1L;
        RiskFactorCreateRequest req = new RiskFactorCreateRequest(
                RiskLevel.COUNTRY, refId, new BigDecimal("0.10"), true
        );

        when(countryRepository.existsById(refId)).thenReturn(true);
        when(riskRepository.findByLevelAndReferenceIdAndActiveTrue(RiskLevel.COUNTRY, refId))
                .thenReturn(List.of(mock(RiskFactorConfiguration.class)));

        assertThrows(ConflictException.class, () -> riskFactorService.create(req));

        verify(countryRepository).existsById(refId);
        verify(riskRepository).findByLevelAndReferenceIdAndActiveTrue(RiskLevel.COUNTRY, refId);
        verifyNoInteractions(metadataMapper);
    }

    @Test
    void createShouldSaveAndReturnResponseWhenOk() {
        long refId = 1L;
        RiskFactorCreateRequest req = new RiskFactorCreateRequest(
                RiskLevel.COUNTRY, refId, new BigDecimal("0.10"), true
        );

        RiskFactorConfiguration entity = mock(RiskFactorConfiguration.class);
        RiskFactorConfiguration saved = mock(RiskFactorConfiguration.class);
        RiskFactorResponse response = mock(RiskFactorResponse.class);

        when(countryRepository.existsById(refId)).thenReturn(true);
        when(riskRepository.findByLevelAndReferenceIdAndActiveTrue(RiskLevel.COUNTRY, refId)).thenReturn(List.of());

        when(metadataMapper.toEntity(req)).thenReturn(entity);
        when(riskRepository.save(entity)).thenReturn(saved);
        when(metadataMapper.toResponse(saved)).thenReturn(response);

        RiskFactorResponse result = riskFactorService.create(req);

        assertSame(response, result);

        verify(countryRepository).existsById(refId);
        verify(riskRepository).findByLevelAndReferenceIdAndActiveTrue(RiskLevel.COUNTRY, refId);
        verify(metadataMapper).toEntity(req);
        verify(riskRepository).save(entity);
        verify(metadataMapper).toResponse(saved);
    }

    @Test
    void getByIdShouldThrowNotFoundWhenMissing() {
        when(riskRepository.findById(RISK_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> riskFactorService.getById(RISK_ID));

        verify(riskRepository).findById(RISK_ID);
        verifyNoInteractions(metadataMapper);
    }

    @Test
    void updateShouldThrowNotFoundWhenMissing() {
        RiskFactorUpdateRequest req = new RiskFactorUpdateRequest(null, null, null, null);
        when(riskRepository.findById(RISK_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> riskFactorService.update(RISK_ID, req));

        verify(riskRepository).findById(RISK_ID);
        verifyNoInteractions(metadataMapper);
    }

    @Test
    void updateShouldApplyUpdateSaveAndReturnResponse() {
        RiskFactorUpdateRequest req = new RiskFactorUpdateRequest(
                RiskLevel.COUNTRY, 2L, new BigDecimal("0.20"), Boolean.FALSE
        );

        RiskFactorConfiguration risk = mock(RiskFactorConfiguration.class);
        RiskFactorConfiguration saved = mock(RiskFactorConfiguration.class);
        RiskFactorResponse response = mock(RiskFactorResponse.class);

        when(riskRepository.findById(RISK_ID)).thenReturn(Optional.of(risk));
        when(riskRepository.save(risk)).thenReturn(saved);
        when(metadataMapper.toResponse(saved)).thenReturn(response);

        RiskFactorResponse result = riskFactorService.update(RISK_ID, req);

        assertSame(response, result);

        verify(riskRepository).findById(RISK_ID);
        verify(metadataMapper).applyUpdate(risk, req);
        verify(riskRepository).save(risk);
        verify(metadataMapper).toResponse(saved);
    }

    @Test
    void listShouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("id").descending());

        RiskFactorConfiguration r1 = mock(RiskFactorConfiguration.class);
        RiskFactorConfiguration r2 = mock(RiskFactorConfiguration.class);
        RiskFactorResponse resp1 = mock(RiskFactorResponse.class);
        RiskFactorResponse resp2 = mock(RiskFactorResponse.class);

        Page<RiskFactorConfiguration> page = new PageImpl<>(List.of(r1, r2), pageable, 2);

        when(riskRepository.findAll(pageable)).thenReturn(page);
        when(metadataMapper.toResponse(r1)).thenReturn(resp1);
        when(metadataMapper.toResponse(r2)).thenReturn(resp2);

        Page<RiskFactorResponse> result = riskFactorService.list(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(List.of(resp1, resp2), result.getContent());

        verify(riskRepository).findAll(pageable);
        verify(metadataMapper).toResponse(r1);
        verify(metadataMapper).toResponse(r2);
    }
}
