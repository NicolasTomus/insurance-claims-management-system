package com.insurance.backend.web.mapper;

import com.insurance.backend.domain.broker.Broker;
import com.insurance.backend.domain.broker.BrokerStatus;
import com.insurance.backend.web.dto.broker.BrokerCreateRequest;
import com.insurance.backend.web.dto.broker.BrokerDetailsResponse;
import com.insurance.backend.web.dto.broker.BrokerSummaryResponse;
import com.insurance.backend.web.dto.broker.BrokerUpdateRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BrokerMapperTest {

    private static final String BROKER_CODE_001 = "BR-001";
    private static final String BROKER_CODE_1 = "BR-1";

    private static final String NAME_BEST_BROKER = "Best Broker";
    private static final String NAME_OLD = "Old";
    private static final String NAME_NEW = "New";
    private static final String NAME_NEW_NAME = "New Name";

    private static final String EMAIL_BROKER = "broker@test.ro";
    private static final String EMAIL_OLD = "old@test.ro";
    private static final String EMAIL_OLD_MAIL = "old@mail.com";
    private static final String EMAIL_NEW_MAIL = "new@mail.com";

    private static final String PHONE_BROKER = "0722222222";
    private static final String PHONE_OLD = "0700000000";
    private static final String PHONE_OLD_SHORT = "000";
    private static final String PHONE_NEW = "0799999999";
    private static final String PHONE_NEW_SHORT = "111";

    private static final BigDecimal COMMISSION_010 = new BigDecimal("0.10");
    private static final BigDecimal COMMISSION_020 = new BigDecimal("0.20");

    private static final BrokerStatus STATUS_ACTIVE = BrokerStatus.ACTIVE;
    private static final BrokerStatus STATUS_INACTIVE = BrokerStatus.INACTIVE;

    private final BrokerMapper brokerMapper = new BrokerMapper();

    @Test
    void toEntityShouldSetActiveStatusAndMapFields() {
        BrokerCreateRequest req = new BrokerCreateRequest(
                BROKER_CODE_001,
                NAME_BEST_BROKER,
                EMAIL_BROKER,
                PHONE_BROKER,
                COMMISSION_010
        );

        Broker broker = brokerMapper.toEntity(req);

        assertNotNull(broker);
        assertEquals(BROKER_CODE_001, broker.getBrokerCode());
        assertEquals(NAME_BEST_BROKER, broker.getName());
        assertEquals(EMAIL_BROKER, broker.getEmail());
        assertEquals(PHONE_BROKER, broker.getPhone());
        assertEquals(STATUS_ACTIVE, broker.getStatus());
        assertEquals(0, COMMISSION_010.compareTo(broker.getCommissionPercentage()));
    }

    @Test
    void applyUpdateShouldUpdateOnlyNonNull() {
        Broker broker = new Broker(
                BROKER_CODE_001,
                NAME_OLD,
                EMAIL_OLD,
                PHONE_OLD,
                STATUS_ACTIVE,
                COMMISSION_010
        );

        BrokerUpdateRequest update = new BrokerUpdateRequest(
                NAME_NEW,
                null,
                PHONE_NEW,
                STATUS_INACTIVE,
                null
        );

        brokerMapper.applyUpdate(broker, update);

        assertEquals(NAME_NEW, broker.getName());
        assertEquals(EMAIL_OLD, broker.getEmail());
        assertEquals(PHONE_NEW, broker.getPhone());
        assertEquals(STATUS_INACTIVE, broker.getStatus());
        assertEquals(0, COMMISSION_010.compareTo(broker.getCommissionPercentage()));
    }

    @Test
    void toDetailsShouldMapFields() {
        Broker broker = new Broker(
                BROKER_CODE_001,
                NAME_BEST_BROKER,
                EMAIL_BROKER,
                PHONE_BROKER,
                STATUS_ACTIVE,
                COMMISSION_010
        );

        BrokerDetailsResponse resp = brokerMapper.toDetails(broker);

        assertNotNull(resp);
        assertNull(resp.id());
        assertEquals(BROKER_CODE_001, resp.brokerCode());
        assertEquals(NAME_BEST_BROKER, resp.name());
        assertEquals(EMAIL_BROKER, resp.email());
        assertEquals(PHONE_BROKER, resp.phone());
        assertEquals(STATUS_ACTIVE, resp.status());
        assertEquals(0, COMMISSION_010.compareTo(resp.commissionPercentage()));
    }

    @Test
    void toSummaryShouldMapFields() {
        Broker broker = new Broker(
                BROKER_CODE_001,
                NAME_BEST_BROKER,
                EMAIL_BROKER,
                PHONE_BROKER,
                STATUS_ACTIVE,
                COMMISSION_010
        );

        BrokerSummaryResponse resp = brokerMapper.toSummary(broker);

        assertNotNull(resp);
        assertNull(resp.id());
        assertEquals(BROKER_CODE_001, resp.brokerCode());
        assertEquals(NAME_BEST_BROKER, resp.name());
        assertEquals(STATUS_ACTIVE, resp.status());
    }

    @Test
    void applyUpdateUpdatesOnlyNonNullFields() {
        BrokerMapper localBrokerMapper = new BrokerMapper();

        Broker broker = new Broker(BROKER_CODE_1, NAME_OLD, EMAIL_OLD_MAIL, PHONE_OLD_SHORT, STATUS_ACTIVE, COMMISSION_010);

        BrokerUpdateRequest req = new BrokerUpdateRequest(
                NAME_NEW_NAME,
                EMAIL_NEW_MAIL,
                PHONE_NEW_SHORT,
                STATUS_INACTIVE,
                COMMISSION_020
        );

        localBrokerMapper.applyUpdate(broker, req);

        assertEquals(NAME_NEW_NAME, broker.getName());
        assertEquals(EMAIL_NEW_MAIL, broker.getEmail());
        assertEquals(PHONE_NEW_SHORT, broker.getPhone());
        assertEquals(STATUS_INACTIVE, broker.getStatus());
        assertEquals(0, COMMISSION_020.compareTo(broker.getCommissionPercentage()));
    }

    @Test
    void applyUpdateAllNullDoesNotChangeAnything() {
        BrokerMapper localBrokerMapper = new BrokerMapper();

        Broker broker = new Broker(BROKER_CODE_1, NAME_OLD, EMAIL_OLD_MAIL, PHONE_OLD_SHORT, STATUS_ACTIVE, COMMISSION_010);

        BrokerUpdateRequest req = new BrokerUpdateRequest(
                null,
                null,
                null,
                null,
                null
        );

        localBrokerMapper.applyUpdate(broker, req);

        assertEquals(NAME_OLD, broker.getName());
        assertEquals(EMAIL_OLD_MAIL, broker.getEmail());
        assertEquals(PHONE_OLD_SHORT, broker.getPhone());
        assertEquals(STATUS_ACTIVE, broker.getStatus());
        assertEquals(0, COMMISSION_010.compareTo(broker.getCommissionPercentage()));
    }
}
