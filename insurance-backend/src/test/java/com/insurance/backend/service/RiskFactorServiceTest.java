package com.insurance.backend.service;

import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;
import com.insurance.backend.domain.metadata.risk.RiskLevel;
import com.insurance.backend.infrastructure.persistence.repository.metadata.risk.RiskFactorConfigurationRepository;
import com.insurance.backend.web.dto.metadata.risk.RiskFactorCreateRequest;
import com.insurance.backend.web.dto.metadata.risk.RiskFactorResponse;
import com.insurance.backend.web.dto.metadata.risk.RiskFactorUpdateRequest;
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

    private static final long RISK_ID = 10L;

    private static final int PAGE_NUMBER = 0;
    private static final int PAGE_SIZE = 10;
    private static final long TOTAL_ELEMENTS = 2L;

    private static final long FACTOR_ID_1 = 1L;
    private static final long FACTOR_ID_2 = 2L;
    private static final long FACTOR_ID_3 = 3L;

    private static final BigDecimal PCT_0500 = new BigDecimal("0.0500");
    private static final BigDecimal PCT_0750 = new BigDecimal("0.0750");
    private static final BigDecimal PCT_1000 = new BigDecimal("0.1000");

    private static final boolean ACTIVE_TRUE = true;
    private static final boolean ACTIVE_FALSE = false;

    private static final RiskLevel LEVEL_COUNTRY = RiskLevel.COUNTRY;
    private static final RiskLevel LEVEL_CITY = RiskLevel.CITY;
    private static final RiskLevel LEVEL_COUNTY = RiskLevel.COUNTY;

    @Mock
    private RiskFactorConfigurationRepository riskRepository;

    @Mock
    private MetadataMapper metadataMapper;

    @InjectMocks
    private RiskFactorService riskFactorService;

    @Test
    void createShouldMapToEntitySaveAndReturnResponse() {
        RiskFactorCreateRequest req = new RiskFactorCreateRequest(
                LEVEL_COUNTRY,
                FACTOR_ID_1,
                PCT_0500,
                ACTIVE_TRUE
        );

        RiskFactorConfiguration entity = mock(RiskFactorConfiguration.class);
        RiskFactorConfiguration saved = mock(RiskFactorConfiguration.class);
        RiskFactorResponse dto = mock(RiskFactorResponse.class);

        when(metadataMapper.toEntity(req)).thenReturn(entity);
        when(riskRepository.save(entity)).thenReturn(saved);
        when(metadataMapper.toResponse(saved)).thenReturn(dto);

        RiskFactorResponse result = riskFactorService.create(req);

        assertSame(dto, result);

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
    void getByIdShouldReturnResponseWhenExists() {
        RiskFactorConfiguration risk = mock(RiskFactorConfiguration.class);
        RiskFactorResponse dto = mock(RiskFactorResponse.class);

        when(riskRepository.findById(RISK_ID)).thenReturn(Optional.of(risk));
        when(metadataMapper.toResponse(risk)).thenReturn(dto);

        RiskFactorResponse result = riskFactorService.getById(RISK_ID);

        assertSame(dto, result);

        verify(riskRepository).findById(RISK_ID);
        verify(metadataMapper).toResponse(risk);
    }

    @Test
    void updateShouldThrowNotFoundWhenMissing() {
        when(riskRepository.findById(RISK_ID)).thenReturn(Optional.empty());

        RiskFactorUpdateRequest req = new RiskFactorUpdateRequest(
                LEVEL_CITY,
                FACTOR_ID_3,
                PCT_1000,
                ACTIVE_FALSE
        );

        assertThrows(NotFoundException.class, () -> riskFactorService.update(RISK_ID, req));

        verify(riskRepository).findById(RISK_ID);
        verifyNoInteractions(metadataMapper);
    }

    @Test
    void updateShouldApplyUpdateSaveAndReturnResponse() {
        RiskFactorConfiguration risk = mock(RiskFactorConfiguration.class);
        RiskFactorConfiguration saved = mock(RiskFactorConfiguration.class);
        RiskFactorResponse dto = mock(RiskFactorResponse.class);

        RiskFactorUpdateRequest req = new RiskFactorUpdateRequest(
                LEVEL_COUNTY,
                FACTOR_ID_2,
                PCT_0750,
                ACTIVE_TRUE
        );

        when(riskRepository.findById(RISK_ID)).thenReturn(Optional.of(risk));
        when(riskRepository.save(risk)).thenReturn(saved);
        when(metadataMapper.toResponse(saved)).thenReturn(dto);

        RiskFactorResponse result = riskFactorService.update(RISK_ID, req);

        assertSame(dto, result);

        verify(riskRepository).findById(RISK_ID);
        verify(metadataMapper).applyUpdate(risk, req);
        verify(riskRepository).save(risk);
        verify(metadataMapper).toResponse(saved);
    }

    @Test
    void listShouldMapEntitiesToResponses() {
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);

        RiskFactorConfiguration r1 = mock(RiskFactorConfiguration.class);
        RiskFactorConfiguration r2 = mock(RiskFactorConfiguration.class);

        RiskFactorResponse dto1 = mock(RiskFactorResponse.class);
        RiskFactorResponse dto2 = mock(RiskFactorResponse.class);

        when(riskRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(r1, r2), pageable, TOTAL_ELEMENTS));
        when(metadataMapper.toResponse(r1)).thenReturn(dto1);
        when(metadataMapper.toResponse(r2)).thenReturn(dto2);

        Page<RiskFactorResponse> result = riskFactorService.list(pageable);

        assertEquals(TOTAL_ELEMENTS, result.getTotalElements());
        assertSame(dto1, result.getContent().get(0));
        assertSame(dto2, result.getContent().get(1));

        verify(riskRepository).findAll(pageable);
        verify(metadataMapper).toResponse(r1);
        verify(metadataMapper).toResponse(r2);
    }
}
