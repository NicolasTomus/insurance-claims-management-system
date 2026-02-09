package com.insurance.backend.service;

import com.insurance.backend.domain.metadata.fee.FeeConfiguration;
import com.insurance.backend.domain.metadata.fee.FeeType;
import com.insurance.backend.infrastructure.persistence.repository.metadata.fee.FeeConfigurationRepository;
import com.insurance.backend.web.dto.metadata.fee.FeeConfigurationCreateRequest;
import com.insurance.backend.web.dto.metadata.fee.FeeConfigurationResponse;
import com.insurance.backend.web.dto.metadata.fee.FeeConfigurationUpdateRequest;
import com.insurance.backend.web.exception.NotFoundException;
import com.insurance.backend.web.mapper.MetadataMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeeConfigurationServiceTest {

    private static final long FEE_ID = 10L;

    private static final int PAGE_NUMBER = 0;
    private static final int PAGE_SIZE = 10;

    private static final long TOTAL_ELEMENTS = 2L;

    private static final String CREATE_NAME_RAW = " Admin Fee ";
    private static final FeeType CREATE_TYPE = FeeType.ADMIN_FEE;
    private static final BigDecimal CREATE_RATE = new BigDecimal("0.1000");
    private static final LocalDate CREATE_VALID_FROM = LocalDate.of(2026, 1, 1);
    private static final LocalDate CREATE_VALID_TO = LocalDate.of(2026, 12, 31);
    private static final boolean CREATE_ACTIVE = true;

    private static final String UPDATE_MISSING_NAME = "New";
    private static final FeeType UPDATE_MISSING_TYPE = FeeType.BROKER_COMMISSION;
    private static final BigDecimal UPDATE_MISSING_RATE = new BigDecimal("0.0500");
    private static final LocalDate UPDATE_MISSING_VALID_FROM = LocalDate.of(2026, 2, 1);
    private static final LocalDate UPDATE_MISSING_VALID_TO = LocalDate.of(2026, 6, 1);
    private static final boolean UPDATE_MISSING_ACTIVE = false;

    private static final String UPDATE_NAME_RAW = " New Name ";
    private static final FeeType UPDATE_TYPE = FeeType.ADMIN_FEE;
    private static final BigDecimal UPDATE_RATE = new BigDecimal("0.1234");
    private static final LocalDate UPDATE_VALID_FROM = LocalDate.of(2026, 3, 1);
    private static final LocalDate UPDATE_VALID_TO = LocalDate.of(2026, 9, 1);
    private static final boolean UPDATE_ACTIVE = true;

    @Mock
    private FeeConfigurationRepository feeRepository;

    @Mock
    private MetadataMapper metadataMapper;

    @InjectMocks
    private FeeConfigurationService feeService;

    @Test
    void createShouldMapToEntitySaveAndReturnResponse() {
        FeeConfigurationCreateRequest req = new FeeConfigurationCreateRequest(
                CREATE_NAME_RAW,
                CREATE_TYPE,
                CREATE_RATE,
                CREATE_VALID_FROM,
                CREATE_VALID_TO,
                CREATE_ACTIVE
        );

        FeeConfiguration entity = mock(FeeConfiguration.class);
        FeeConfiguration saved = mock(FeeConfiguration.class);
        FeeConfigurationResponse dto = mock(FeeConfigurationResponse.class);

        when(metadataMapper.toEntity(req)).thenReturn(entity);
        when(feeRepository.save(entity)).thenReturn(saved);
        when(metadataMapper.toResponse(saved)).thenReturn(dto);

        FeeConfigurationResponse result = feeService.create(req);

        assertSame(dto, result);

        verify(metadataMapper).toEntity(req);
        verify(feeRepository).save(entity);
        verify(metadataMapper).toResponse(saved);
    }

    @Test
    void getByIdShouldThrowNotFoundWhenMissing() {
        when(feeRepository.findById(FEE_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> feeService.getById(FEE_ID));

        verify(feeRepository).findById(FEE_ID);
        verifyNoInteractions(metadataMapper);
    }

    @Test
    void getByIdShouldReturnResponseWhenExists() {
        FeeConfiguration fee = mock(FeeConfiguration.class);
        FeeConfigurationResponse dto = mock(FeeConfigurationResponse.class);

        when(feeRepository.findById(FEE_ID)).thenReturn(Optional.of(fee));
        when(metadataMapper.toResponse(fee)).thenReturn(dto);

        FeeConfigurationResponse result = feeService.getById(FEE_ID);

        assertSame(dto, result);

        verify(feeRepository).findById(FEE_ID);
        verify(metadataMapper).toResponse(fee);
    }

    @Test
    void updateShouldThrowNotFoundWhenMissing() {
        when(feeRepository.findById(FEE_ID)).thenReturn(Optional.empty());

        FeeConfigurationUpdateRequest req = new FeeConfigurationUpdateRequest(
                UPDATE_MISSING_NAME,
                UPDATE_MISSING_TYPE,
                UPDATE_MISSING_RATE,
                UPDATE_MISSING_VALID_FROM,
                UPDATE_MISSING_VALID_TO,
                UPDATE_MISSING_ACTIVE
        );

        assertThrows(NotFoundException.class, () -> feeService.update(FEE_ID, req));

        verify(feeRepository).findById(FEE_ID);
        verifyNoInteractions(metadataMapper);
    }

    @Test
    void updateShouldApplyUpdateSaveAndReturnResponse() {
        FeeConfiguration fee = mock(FeeConfiguration.class);
        FeeConfiguration saved = mock(FeeConfiguration.class);
        FeeConfigurationResponse dto = mock(FeeConfigurationResponse.class);

        FeeConfigurationUpdateRequest req = new FeeConfigurationUpdateRequest(
                UPDATE_NAME_RAW,
                UPDATE_TYPE,
                UPDATE_RATE,
                UPDATE_VALID_FROM,
                UPDATE_VALID_TO,
                UPDATE_ACTIVE
        );

        when(feeRepository.findById(FEE_ID)).thenReturn(Optional.of(fee));
        when(feeRepository.save(fee)).thenReturn(saved);
        when(metadataMapper.toResponse(saved)).thenReturn(dto);

        FeeConfigurationResponse result = feeService.update(FEE_ID, req);

        assertSame(dto, result);

        verify(feeRepository).findById(FEE_ID);
        verify(metadataMapper).applyUpdate(fee, req);
        verify(feeRepository).save(fee);
        verify(metadataMapper).toResponse(saved);
    }

    @Test
    void listShouldMapEntitiesToResponses() {
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);

        FeeConfiguration f1 = mock(FeeConfiguration.class);
        FeeConfiguration f2 = mock(FeeConfiguration.class);

        FeeConfigurationResponse r1 = mock(FeeConfigurationResponse.class);
        FeeConfigurationResponse r2 = mock(FeeConfigurationResponse.class);

        when(feeRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(f1, f2), pageable, TOTAL_ELEMENTS));
        when(metadataMapper.toResponse(f1)).thenReturn(r1);
        when(metadataMapper.toResponse(f2)).thenReturn(r2);

        Page<FeeConfigurationResponse> result = feeService.list(pageable);

        assertEquals(TOTAL_ELEMENTS, result.getTotalElements());
        assertSame(r1, result.getContent().get(0));
        assertSame(r2, result.getContent().get(1));

        verify(feeRepository).findAll(pageable);
        verify(metadataMapper).toResponse(f1);
        verify(metadataMapper).toResponse(f2);
    }
}
