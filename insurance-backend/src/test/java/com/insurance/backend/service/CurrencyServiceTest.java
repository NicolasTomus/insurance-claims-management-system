package com.insurance.backend.service;

import com.insurance.backend.domain.metadata.currency.Currency;
import com.insurance.backend.infrastructure.persistence.repository.metadata.currency.CurrencyRepository;
import com.insurance.backend.web.dto.metadata.currency.CurrencyCreateRequest;
import com.insurance.backend.web.dto.metadata.currency.CurrencyResponse;
import com.insurance.backend.web.dto.metadata.currency.CurrencyUpdateRequest;
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
class CurrencyServiceTest {

    private static final long CURRENCY_ID = 10L;

    private static final int PAGE_NUMBER = 0;
    private static final int PAGE_SIZE = 10;

    private static final String CODE_RAW = " eur ";
    private static final String CODE_NORM = "EUR";

    private static final String NAME_RAW = "  Euro  ";
    private static final String NAME_TRIM = "Euro";

    private static final BigDecimal RATE = new BigDecimal("1.00");

    private static final String UPDATE_NAME_RAW = " New ";
    private static final String UPDATE_NAME_TRIM = "New";
    private static final BigDecimal UPDATE_RATE = new BigDecimal("2.00");
    private static final boolean ACTIVE_TRUE = true;
    private static final boolean ACTIVE_FALSE = false;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private MetadataMapper metadataMapper;

    @InjectMocks
    private CurrencyService currencyService;

    @Test
    void createShouldThrowConflictWhenCodeAlreadyExists() {
        CurrencyCreateRequest req = new CurrencyCreateRequest(CODE_RAW, NAME_RAW, RATE, ACTIVE_TRUE);

        when(currencyRepository.existsByCode(CODE_NORM)).thenReturn(true);

        assertThrows(ConflictException.class, () -> currencyService.create(req));

        verify(currencyRepository).existsByCode(CODE_NORM);
        verifyNoMoreInteractions(currencyRepository);
        verifyNoInteractions(metadataMapper);
    }

    @Test
    void createShouldSaveAndReturnResponseWhenOk() {
        CurrencyCreateRequest req = new CurrencyCreateRequest(CODE_RAW, NAME_RAW, RATE, ACTIVE_TRUE);

        Currency saved = mock(Currency.class);
        CurrencyResponse dto = mock(CurrencyResponse.class);

        when(currencyRepository.existsByCode(CODE_NORM)).thenReturn(false);
        when(currencyRepository.save(any(Currency.class))).thenReturn(saved);
        when(metadataMapper.toResponse(saved)).thenReturn(dto);

        CurrencyResponse result = currencyService.create(req);

        assertNotNull(result);
        assertSame(dto, result);

        verify(currencyRepository).existsByCode(CODE_NORM);
        verify(currencyRepository).save(argThat(c ->
                CODE_NORM.equals(c.getCode())
                        && NAME_TRIM.equals(c.getName())
                        && RATE.compareTo(c.getExchangeRateToBase()) == 0
                        && c.isActive()
        ));
        verify(metadataMapper).toResponse(saved);
    }

    @Test
    void getByIdShouldThrowNotFoundWhenMissing() {
        when(currencyRepository.findById(CURRENCY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> currencyService.getById(CURRENCY_ID));

        verify(currencyRepository).findById(CURRENCY_ID);
        verifyNoInteractions(metadataMapper);
    }

    @Test
    void getByIdShouldReturnResponseWhenExists() {
        Currency currency = mock(Currency.class);
        CurrencyResponse dto = mock(CurrencyResponse.class);

        when(currencyRepository.findById(CURRENCY_ID)).thenReturn(Optional.of(currency));
        when(metadataMapper.toResponse(currency)).thenReturn(dto);

        CurrencyResponse result = currencyService.getById(CURRENCY_ID);

        assertSame(dto, result);

        verify(currencyRepository).findById(CURRENCY_ID);
        verify(metadataMapper).toResponse(currency);
    }

    @Test
    void updateShouldThrowNotFoundWhenMissing() {
        when(currencyRepository.findById(CURRENCY_ID)).thenReturn(Optional.empty());

        CurrencyUpdateRequest req = new CurrencyUpdateRequest(UPDATE_NAME_TRIM, UPDATE_RATE, ACTIVE_FALSE);

        assertThrows(NotFoundException.class, () -> currencyService.update(CURRENCY_ID, req));

        verify(currencyRepository).findById(CURRENCY_ID);
        verifyNoInteractions(metadataMapper);
    }

    @Test
    void updateShouldApplyUpdateSaveAndReturnResponse() {
        Currency currency = mock(Currency.class);
        Currency saved = mock(Currency.class);
        CurrencyResponse dto = mock(CurrencyResponse.class);

        CurrencyUpdateRequest req = new CurrencyUpdateRequest(UPDATE_NAME_RAW, UPDATE_RATE, ACTIVE_FALSE);

        when(currencyRepository.findById(CURRENCY_ID)).thenReturn(Optional.of(currency));
        when(currencyRepository.save(currency)).thenReturn(saved);
        when(metadataMapper.toResponse(saved)).thenReturn(dto);

        CurrencyResponse result = currencyService.update(CURRENCY_ID, req);

        assertSame(dto, result);

        verify(currencyRepository).findById(CURRENCY_ID);
        verify(metadataMapper).applyUpdate(currency, req);
        verify(currencyRepository).save(currency);
        verify(metadataMapper).toResponse(saved);
    }

    @Test
    void listShouldMapEntitiesToResponses() {
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);

        Currency c1 = mock(Currency.class);
        Currency c2 = mock(Currency.class);

        CurrencyResponse r1 = mock(CurrencyResponse.class);
        CurrencyResponse r2 = mock(CurrencyResponse.class);

        when(currencyRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(c1, c2), pageable, 2));
        when(metadataMapper.toResponse(c1)).thenReturn(r1);
        when(metadataMapper.toResponse(c2)).thenReturn(r2);

        Page<CurrencyResponse> result = currencyService.list(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertSame(r1, result.getContent().get(0));
        assertSame(r2, result.getContent().get(1));

        verify(currencyRepository).findAll(pageable);
        verify(metadataMapper).toResponse(c1);
        verify(metadataMapper).toResponse(c2);
    }
}
