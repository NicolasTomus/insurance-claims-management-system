package com.insurance.backend.web.mapper;

import com.insurance.backend.domain.metadata.currency.Currency;
import com.insurance.backend.web.dto.metadata.currency.CurrencyCreateRequest;
import com.insurance.backend.web.dto.metadata.currency.CurrencyResponse;
import com.insurance.backend.web.dto.metadata.currency.CurrencyUpdateRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MetadataMapperTest {

    private static final String CODE_RAW = " eur ";
    private static final String CODE_NORM = "EUR";

    private static final String NAME_RAW = "  Euro  ";
    private static final String NAME_TRIM = "Euro";

    private static final String NAME_OLD = "Old";
    private static final String NAME_NEW_RAW = "  New Name  ";
    private static final String NAME_NEW_TRIM = "New Name";

    private static final String NAME_UPDATED_RAW = "  Euro Updated  ";
    private static final String NAME_UPDATED_TRIM = "Euro Updated";

    private static final BigDecimal RATE_100 = new BigDecimal("1.00");
    private static final BigDecimal RATE_125 = new BigDecimal("1.25");

    private static final boolean ACTIVE_TRUE = true;
    private static final boolean ACTIVE_FALSE = false;

    private final MetadataMapper metadataMapper = new MetadataMapper();

    @Test
    void currencyToEntityShouldTrimAndUppercaseCodeAndTrimName() {
        CurrencyCreateRequest req = new CurrencyCreateRequest(
                CODE_RAW,
                NAME_RAW,
                RATE_100,
                ACTIVE_TRUE
        );

        Currency result = metadataMapper.toEntity(req);

        assertNotNull(result);
        assertEquals(CODE_NORM, result.getCode());
        assertEquals(NAME_TRIM, result.getName());
        assertEquals(0, RATE_100.compareTo(result.getExchangeRateToBase()));
        assertTrue(result.isActive());
    }

    @Test
    void currencyApplyUpdateShouldUpdateOnlyNonNullAndTrimName() {
        Currency currency = new Currency(CODE_NORM, NAME_OLD, RATE_100, ACTIVE_TRUE);

        CurrencyUpdateRequest req = new CurrencyUpdateRequest(
                NAME_NEW_RAW,
                null,
                ACTIVE_FALSE
        );

        metadataMapper.applyUpdate(currency, req);

        assertEquals(NAME_NEW_TRIM, currency.getName());
        assertEquals(0, RATE_100.compareTo(currency.getExchangeRateToBase()));
        assertFalse(currency.isActive());
    }

    @Test
    void currencyToResponseShouldMapFields() {
        Currency currency = new Currency(CODE_NORM, NAME_TRIM, RATE_100, ACTIVE_TRUE);

        CurrencyResponse resp = metadataMapper.toResponse(currency);

        assertNotNull(resp);
        assertNull(resp.id());
        assertEquals(CODE_NORM, resp.code());
        assertEquals(NAME_TRIM, resp.name());
        assertEquals(0, RATE_100.compareTo(resp.exchangeRateToBase()));
        assertTrue(resp.active());
    }

    @Test
    void applyUpdateCurrencyUpdatesOnlyNonNullFieldsAndTrimsName() {
        MetadataMapper localMetadataMapper = new MetadataMapper();

        Currency currency = new Currency(CODE_NORM, NAME_TRIM, RATE_100, ACTIVE_TRUE);

        CurrencyUpdateRequest req = new CurrencyUpdateRequest(
                NAME_UPDATED_RAW,
                RATE_125,
                ACTIVE_FALSE
        );

        localMetadataMapper.applyUpdate(currency, req);

        assertEquals(NAME_UPDATED_TRIM, currency.getName());
        assertEquals(0, RATE_125.compareTo(currency.getExchangeRateToBase()));
        assertFalse(currency.isActive());
    }

    @Test
    void applyUpdateCurrencyAllNullDoesNotChangeAnything() {
        MetadataMapper localMetadataMapper = new MetadataMapper();

        Currency currency = new Currency(CODE_NORM, NAME_TRIM, RATE_100, ACTIVE_TRUE);

        CurrencyUpdateRequest req = new CurrencyUpdateRequest(null, null, null);

        localMetadataMapper.applyUpdate(currency, req);

        assertEquals(NAME_TRIM, currency.getName());
        assertEquals(0, RATE_100.compareTo(currency.getExchangeRateToBase()));
        assertTrue(currency.isActive());
    }
}
