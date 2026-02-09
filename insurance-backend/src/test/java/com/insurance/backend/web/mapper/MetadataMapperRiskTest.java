package com.insurance.backend.web.mapper;

import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;
import com.insurance.backend.domain.metadata.risk.RiskLevel;
import com.insurance.backend.web.dto.metadata.risk.RiskFactorCreateRequest;
import com.insurance.backend.web.dto.metadata.risk.RiskFactorResponse;
import com.insurance.backend.web.dto.metadata.risk.RiskFactorUpdateRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MetadataMapperRiskTest {

    private static final long REF_ID_1 = 1L;
    private static final long REF_ID_2 = 2L;
    private static final long REF_ID_3 = 3L;
    private static final long REF_ID_10 = 10L;
    private static final long REF_ID_99 = 99L;

    private static final BigDecimal PCT_00500 = new BigDecimal("0.0500");
    private static final BigDecimal PCT_01000 = new BigDecimal("0.1000");
    private static final BigDecimal PCT_01200 = new BigDecimal("0.1200");

    private static final boolean ACTIVE_TRUE = true;
    private static final boolean ACTIVE_FALSE = false;

    private final MetadataMapper metadataMapper = new MetadataMapper();

    @Test
    void riskToEntityShouldMapAllFields() {
        RiskFactorCreateRequest req = new RiskFactorCreateRequest(
                RiskLevel.COUNTRY,
                REF_ID_1,
                PCT_00500,
                ACTIVE_TRUE
        );

        RiskFactorConfiguration risk = metadataMapper.toEntity(req);

        assertNotNull(risk);
        assertEquals(RiskLevel.COUNTRY, risk.getLevel());
        assertEquals(REF_ID_1, risk.getReferenceId());
        assertEquals(0, PCT_00500.compareTo(risk.getAdjustmentPercentage()));
        assertTrue(risk.isActive());
    }

    @Test
    void riskApplyUpdateShouldUpdateOnlyNonNull() {
        RiskFactorConfiguration risk = new RiskFactorConfiguration(
                RiskLevel.COUNTRY,
                REF_ID_1,
                PCT_00500,
                ACTIVE_TRUE
        );

        RiskFactorUpdateRequest update = new RiskFactorUpdateRequest(
                null,
                REF_ID_2,
                null,
                ACTIVE_FALSE
        );

        metadataMapper.applyUpdate(risk, update);

        assertEquals(RiskLevel.COUNTRY, risk.getLevel());
        assertEquals(REF_ID_2, risk.getReferenceId());
        assertEquals(0, PCT_00500.compareTo(risk.getAdjustmentPercentage()));
        assertFalse(risk.isActive());
    }

    @Test
    void riskToResponseShouldMapFields() {
        RiskFactorConfiguration risk = new RiskFactorConfiguration(
                RiskLevel.CITY,
                REF_ID_3,
                PCT_01000,
                ACTIVE_TRUE
        );

        RiskFactorResponse resp = metadataMapper.toResponse(risk);

        assertNotNull(resp);
        assertNull(resp.id());
        assertEquals(RiskLevel.CITY, resp.level());
        assertEquals(REF_ID_3, resp.referenceId());
        assertEquals(0, PCT_01000.compareTo(resp.adjustmentPercentage()));
        assertTrue(resp.active());
    }

    @Test
    void applyUpdateRiskUpdatesOnlyNonNullFields() {
        MetadataMapper localMetadataMapper = new MetadataMapper();

        RiskFactorConfiguration risk = new RiskFactorConfiguration(
                RiskLevel.CITY,
                REF_ID_10,
                PCT_00500,
                ACTIVE_TRUE
        );

        RiskFactorUpdateRequest req = new RiskFactorUpdateRequest(
                RiskLevel.COUNTRY,
                REF_ID_99,
                PCT_01200,
                ACTIVE_FALSE
        );

        localMetadataMapper.applyUpdate(risk, req);

        assertEquals(RiskLevel.COUNTRY, risk.getLevel());
        assertEquals(REF_ID_99, risk.getReferenceId());
        assertEquals(0, PCT_01200.compareTo(risk.getAdjustmentPercentage()));
        assertFalse(risk.isActive());
    }

    @Test
    void applyUpdateRiskAllNullDoesNotChangeAnything() {
        MetadataMapper localMetadataMapper = new MetadataMapper();

        RiskFactorConfiguration risk = new RiskFactorConfiguration(
                RiskLevel.CITY,
                REF_ID_10,
                PCT_00500,
                ACTIVE_TRUE
        );

        RiskFactorUpdateRequest req = new RiskFactorUpdateRequest(null, null, null, null);

        localMetadataMapper.applyUpdate(risk, req);

        assertEquals(RiskLevel.CITY, risk.getLevel());
        assertEquals(REF_ID_10, risk.getReferenceId());
        assertEquals(0, PCT_00500.compareTo(risk.getAdjustmentPercentage()));
        assertTrue(risk.isActive());
    }
}
