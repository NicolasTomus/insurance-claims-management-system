package com.insurance.backend.web.mapper;

import com.insurance.backend.domain.broker.Broker;
import com.insurance.backend.domain.building.Building;
import com.insurance.backend.domain.client.Client;
import com.insurance.backend.domain.metadata.currency.Currency;
import com.insurance.backend.domain.policy.Policy;
import com.insurance.backend.domain.policy.PolicyStatus;
import com.insurance.backend.web.dto.broker.BrokerSummaryResponse;
import com.insurance.backend.web.dto.building.BuildingDetailsResponse;
import com.insurance.backend.web.dto.client.ClientSummaryResponse;
import com.insurance.backend.web.dto.policy.PolicyCreateDraftRequest;
import com.insurance.backend.web.dto.policy.PolicyDetailsResponse;
import com.insurance.backend.web.dto.policy.PolicySummaryResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PolicyMapperTest {

    private static final String POLICY_NUMBER_1 = "POL-1";
    private static final String POLICY_NUMBER_7 = "POL-7";

    private static final long CLIENT_ID = 10L;
    private static final long BUILDING_ID = 20L;
    private static final long BROKER_ID = 30L;
    private static final long CURRENCY_ID = 40L;

    private static final long POLICY_ID_1 = 1L;
    private static final long POLICY_ID_7 = 7L;

    private static final LocalDate START_DATE = LocalDate.of(2026, 2, 10);
    private static final LocalDate END_DATE = LocalDate.of(2026, 3, 10);

    private static final BigDecimal BASE_123_45 = new BigDecimal("123.45");
    private static final BigDecimal BASE_100_00 = new BigDecimal("100.00");
    private static final BigDecimal FINAL_150_00 = new BigDecimal("150.00");
    private static final BigDecimal FINAL_130_00 = new BigDecimal("130.00");

    private static final String CODE_EUR = "EUR";
    private static final String CODE_RON = "RON";

    private final ClientMapper clientMapper = mock(ClientMapper.class);
    private final BuildingMapper buildingMapper = mock(BuildingMapper.class);
    private final BrokerMapper brokerMapper = mock(BrokerMapper.class);

    private final PolicyMapper policyMapper = new PolicyMapper(clientMapper, buildingMapper, brokerMapper);

    @Test
    void toEntityShouldMapCoreFieldsFromRequestAndRelations() {
        PolicyCreateDraftRequest req = new PolicyCreateDraftRequest(
                CLIENT_ID,
                BUILDING_ID,
                BROKER_ID,
                START_DATE,
                END_DATE,
                BASE_123_45,
                CURRENCY_ID,
                null
        );

        Client client = mock(Client.class);
        Building building = mock(Building.class);
        Broker broker = mock(Broker.class);
        Currency currency = mock(Currency.class);

        Policy policy = policyMapper.toEntity(POLICY_NUMBER_1, req, client, building, broker, currency);

        assertNotNull(policy);
        assertEquals(POLICY_NUMBER_1, policy.getPolicyNumber());
        assertSame(client, policy.getClient());
        assertSame(building, policy.getBuilding());
        assertSame(broker, policy.getBroker());
        assertSame(currency, policy.getCurrency());

        assertEquals(req.startDate(), policy.getStartDate());
        assertEquals(req.endDate(), policy.getEndDate());
        assertEquals(req.basePremiumAmount(), policy.getBasePremiumAmount());
    }

    @Test
    void toSummaryShouldMapFieldsAndCurrencyCodeNullSafe() {
        Policy policy = mock(Policy.class);
        Currency currency = mock(Currency.class);

        when(policy.getId()).thenReturn(POLICY_ID_1);
        when(policy.getPolicyNumber()).thenReturn(POLICY_NUMBER_1);
        when(policy.getStatus()).thenReturn(PolicyStatus.DRAFT);
        when(policy.getStartDate()).thenReturn(START_DATE);
        when(policy.getEndDate()).thenReturn(END_DATE);
        when(policy.getFinalPremiumAmount()).thenReturn(FINAL_150_00);
        when(policy.getCurrency()).thenReturn(currency);
        when(currency.getCode()).thenReturn(CODE_EUR);

        PolicySummaryResponse dto = policyMapper.toSummary(policy);

        assertNotNull(dto);
        assertEquals(POLICY_ID_1, dto.id());
        assertEquals(POLICY_NUMBER_1, dto.policyNumber());
        assertEquals(PolicyStatus.DRAFT, dto.status());
        assertEquals(FINAL_150_00, dto.finalPremiumAmount());
        assertEquals(CODE_EUR, dto.currencyCode());

        when(policy.getCurrency()).thenReturn(null);
        PolicySummaryResponse dto2 = policyMapper.toSummary(policy);
        assertNull(dto2.currencyCode());
    }

    @Test
    void toDetailsShouldMapFieldsAndDelegateToOtherMappers() {
        Policy policy = mock(Policy.class);

        Client client = mock(Client.class);
        Building building = mock(Building.class);
        Broker broker = mock(Broker.class);
        Currency currency = mock(Currency.class);

        when(policy.getId()).thenReturn(POLICY_ID_7);
        when(policy.getPolicyNumber()).thenReturn(POLICY_NUMBER_7);
        when(policy.getStatus()).thenReturn(PolicyStatus.ACTIVE);
        when(policy.getStartDate()).thenReturn(START_DATE);
        when(policy.getEndDate()).thenReturn(END_DATE);
        when(policy.getBasePremiumAmount()).thenReturn(BASE_100_00);
        when(policy.getFinalPremiumAmount()).thenReturn(FINAL_130_00);
        when(policy.getCurrency()).thenReturn(currency);
        when(currency.getCode()).thenReturn(CODE_RON);

        when(policy.getClient()).thenReturn(client);
        when(policy.getBuilding()).thenReturn(building);
        when(policy.getBroker()).thenReturn(broker);

        ClientSummaryResponse clientDto = mock(ClientSummaryResponse.class);
        BuildingDetailsResponse buildingDto = mock(BuildingDetailsResponse.class);
        BrokerSummaryResponse brokerDto = mock(BrokerSummaryResponse.class);

        when(clientMapper.toSummary(client)).thenReturn(clientDto);
        when(buildingMapper.toDetails(building)).thenReturn(buildingDto);
        when(brokerMapper.toSummary(broker)).thenReturn(brokerDto);

        PolicyDetailsResponse dto = policyMapper.toDetails(policy);

        assertNotNull(dto);
        assertEquals(POLICY_ID_7, dto.id());
        assertEquals(POLICY_NUMBER_7, dto.policyNumber());
        assertEquals(PolicyStatus.ACTIVE, dto.status());
        assertEquals(CODE_RON, dto.currencyCode());

        assertSame(clientDto, dto.client());
        assertSame(buildingDto, dto.building());
        assertSame(brokerDto, dto.broker());

        verify(clientMapper).toSummary(client);
        verify(buildingMapper).toDetails(building);
        verify(brokerMapper).toSummary(broker);
    }

    @Test
    void toDetailsShouldMapCurrencyCodeNullSafe() {
        Policy policy = mock(Policy.class);

        Client client = mock(Client.class);
        Building building = mock(Building.class);
        Broker broker = mock(Broker.class);

        when(policy.getId()).thenReturn(POLICY_ID_7);
        when(policy.getPolicyNumber()).thenReturn(POLICY_NUMBER_7);
        when(policy.getStatus()).thenReturn(PolicyStatus.ACTIVE);
        when(policy.getStartDate()).thenReturn(START_DATE);
        when(policy.getEndDate()).thenReturn(END_DATE);
        when(policy.getBasePremiumAmount()).thenReturn(BASE_100_00);
        when(policy.getFinalPremiumAmount()).thenReturn(FINAL_130_00);

        when(policy.getCurrency()).thenReturn(null);

        when(policy.getClient()).thenReturn(client);
        when(policy.getBuilding()).thenReturn(building);
        when(policy.getBroker()).thenReturn(broker);

        when(clientMapper.toSummary(client)).thenReturn(mock(ClientSummaryResponse.class));
        when(buildingMapper.toDetails(building)).thenReturn(mock(BuildingDetailsResponse.class));
        when(brokerMapper.toSummary(broker)).thenReturn(mock(BrokerSummaryResponse.class));

        PolicyDetailsResponse dto = policyMapper.toDetails(policy);

        assertNotNull(dto);
        assertNull(dto.currencyCode());
    }

}
