package com.insurance.backend.web.mapper;

import com.insurance.backend.domain.metadata.fee.FeeConfiguration;
import com.insurance.backend.domain.metadata.fee.FeeType;
import com.insurance.backend.web.dto.metadata.fee.FeeConfigurationCreateRequest;
import com.insurance.backend.web.dto.metadata.fee.FeeConfigurationResponse;
import com.insurance.backend.web.dto.metadata.fee.FeeConfigurationUpdateRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MetadataMapperFeeTest {

    private static final String NAME_ADMIN_RAW = "  Admin Fee  ";
    private static final String NAME_ADMIN = "Admin Fee";

    private static final String NAME_OLD = "Old";
    private static final String NAME_OLD_NAME = "Old Name";

    private static final String NAME_NEW_RAW = "  New Name  ";
    private static final String NAME_NEW = "New Name";

    private static final BigDecimal PCT_01000 = new BigDecimal("0.1000");
    private static final BigDecimal PCT_02500 = new BigDecimal("0.2500");

    private static final LocalDate DATE_2026_01_01 = LocalDate.of(2026, 1, 1);
    private static final LocalDate DATE_2026_12_31 = LocalDate.of(2026, 12, 31);
    private static final LocalDate DATE_2026_02_01 = LocalDate.of(2026, 2, 1);
    private static final LocalDate DATE_2026_11_30 = LocalDate.of(2026, 11, 30);

    private static final FeeType TYPE_ADMIN = FeeType.ADMIN_FEE;
    private static final FeeType TYPE_BROKER = FeeType.BROKER_COMMISSION;

    private static final boolean ACTIVE_TRUE = true;
    private static final boolean ACTIVE_FALSE = false;

    private final MetadataMapper metadataMapper = new MetadataMapper();

    @Test
    void feeToEntityShouldTrimNameAndMapAllFields() {
        FeeConfigurationCreateRequest req = new FeeConfigurationCreateRequest(
                NAME_ADMIN_RAW,
                TYPE_ADMIN,
                PCT_01000,
                DATE_2026_01_01,
                DATE_2026_12_31,
                ACTIVE_TRUE
        );

        FeeConfiguration fee = metadataMapper.toEntity(req);

        assertNotNull(fee);
        assertEquals(NAME_ADMIN, fee.getName());
        assertEquals(TYPE_ADMIN, fee.getType());
        assertEquals(0, PCT_01000.compareTo(fee.getPercentage()));
        assertEquals(DATE_2026_01_01, fee.getEffectiveFrom());
        assertEquals(DATE_2026_12_31, fee.getEffectiveTo());
        assertTrue(fee.isActive());
    }

    @Test
    void feeApplyUpdateShouldUpdateOnlyNonNullAndTrimName() {
        FeeConfiguration fee = new FeeConfiguration(
                NAME_OLD,
                TYPE_ADMIN,
                PCT_01000,
                DATE_2026_01_01,
                DATE_2026_12_31,
                ACTIVE_TRUE
        );

        FeeConfigurationUpdateRequest update = new FeeConfigurationUpdateRequest(
                NAME_NEW_RAW,
                null,
                null,
                DATE_2026_02_01,
                null,
                ACTIVE_FALSE
        );

        metadataMapper.applyUpdate(fee, update);

        assertEquals(NAME_NEW, fee.getName());
        assertEquals(TYPE_ADMIN, fee.getType());
        assertEquals(0, PCT_01000.compareTo(fee.getPercentage()));
        assertEquals(DATE_2026_02_01, fee.getEffectiveFrom());
        assertEquals(DATE_2026_12_31, fee.getEffectiveTo());
        assertFalse(fee.isActive());
    }

    @Test
    void feeToResponseShouldMapFields() {
        FeeConfiguration fee = new FeeConfiguration(
                NAME_ADMIN,
                TYPE_ADMIN,
                PCT_01000,
                DATE_2026_01_01,
                DATE_2026_12_31,
                ACTIVE_TRUE
        );

        FeeConfigurationResponse resp = metadataMapper.toResponse(fee);

        assertNotNull(resp);
        assertNull(resp.id());
        assertEquals(NAME_ADMIN, resp.name());
        assertEquals(TYPE_ADMIN, resp.type());
        assertEquals(0, PCT_01000.compareTo(resp.percentage()));
        assertEquals(DATE_2026_01_01, resp.effectiveFrom());
        assertEquals(DATE_2026_12_31, resp.effectiveTo());
        assertTrue(resp.active());
    }

    @Test
    void applyUpdateFeeUpdatesOnlyNonNullFieldsAndTrimsName() {
        MetadataMapper localMetadataMapper = new MetadataMapper();

        FeeConfiguration fee = new FeeConfiguration(
                NAME_OLD_NAME,
                TYPE_ADMIN,
                PCT_01000,
                DATE_2026_01_01,
                DATE_2026_12_31,
                ACTIVE_TRUE
        );

        FeeConfigurationUpdateRequest req = new FeeConfigurationUpdateRequest(
                NAME_NEW_RAW,
                TYPE_BROKER,
                PCT_02500,
                DATE_2026_02_01,
                DATE_2026_11_30,
                ACTIVE_FALSE
        );

        localMetadataMapper.applyUpdate(fee, req);

        assertEquals(NAME_NEW, fee.getName());
        assertEquals(TYPE_BROKER, fee.getType());
        assertEquals(0, PCT_02500.compareTo(fee.getPercentage()));
        assertEquals(DATE_2026_02_01, fee.getEffectiveFrom());
        assertEquals(DATE_2026_11_30, fee.getEffectiveTo());
        assertFalse(fee.isActive());
    }

    @Test
    void applyUpdateFeeAllNullDoesNotChangeAnything() {
        MetadataMapper localMetadataMapper = new MetadataMapper();

        FeeConfiguration fee = new FeeConfiguration(
                NAME_OLD_NAME,
                TYPE_ADMIN,
                PCT_01000,
                DATE_2026_01_01,
                DATE_2026_12_31,
                ACTIVE_TRUE
        );

        FeeConfigurationUpdateRequest req = new FeeConfigurationUpdateRequest(
                null, null, null, null, null, null
        );

        localMetadataMapper.applyUpdate(fee, req);

        assertEquals(NAME_OLD_NAME, fee.getName());
        assertEquals(TYPE_ADMIN, fee.getType());
        assertEquals(0, PCT_01000.compareTo(fee.getPercentage()));
        assertEquals(DATE_2026_01_01, fee.getEffectiveFrom());
        assertEquals(DATE_2026_12_31, fee.getEffectiveTo());
        assertTrue(fee.isActive());
    }
}
