package com.insurance.backend.service;

import com.insurance.backend.domain.broker.Broker;
import com.insurance.backend.domain.metadata.currency.Currency;
import com.insurance.backend.domain.policy.Policy;
import com.insurance.backend.infrastructure.persistence.repository.policy.PolicyRepository;
import com.insurance.backend.web.dto.report.PolicyAggregateResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    private static final long BROKER_ID_1 = 1L;
    private static final long BROKER_ID_2 = 2L;

    private static final String BROKER_KEY_1 = "1";
    private static final String BROKER_KEY_2 = "2";

    private static final long COUNT_1 = 1L;
    private static final long COUNT_3 = 3L;

    private static final int EXPECTED_SIZE_1 = 1;

    private static final LocalDate START_2026_01_10 = LocalDate.of(2026, 1, 10);
    private static final LocalDate END_2026_02_10 = LocalDate.of(2026, 2, 10);
    private static final LocalDate START_2026_01_01 = LocalDate.of(2026, 1, 1);
    private static final LocalDate END_2026_12_31 = LocalDate.of(2026, 12, 31);

    private static final LocalDate DATE_2026_01_01 = LocalDate.of(2026, 1, 1);
    private static final LocalDate DATE_2026_02_01 = LocalDate.of(2026, 2, 1);
    private static final LocalDate DATE_2026_02_02 = LocalDate.of(2026, 2, 2);
    private static final LocalDate DATE_2026_03_01 = LocalDate.of(2026, 3, 1);

    private static final BigDecimal AMOUNT_5 = new BigDecimal("5.00");
    private static final BigDecimal AMOUNT_7 = new BigDecimal("7.00");
    private static final BigDecimal AMOUNT_10 = new BigDecimal("10.00");
    private static final BigDecimal RATE_2 = new BigDecimal("2.00");
    private static final BigDecimal TOTAL_24 = new BigDecimal("24.00");

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void policiesByBrokerShouldSkipPoliciesBeforeStartDate() {
        Broker broker = mock(Broker.class);
        when(broker.getId()).thenReturn(BROKER_ID_1);

        Policy beforeStart = mock(Policy.class);
        when(beforeStart.getStartDate()).thenReturn(DATE_2026_01_01);

        Policy inRange = mock(Policy.class);
        when(inRange.getStartDate()).thenReturn(START_2026_01_10);
        when(inRange.getBroker()).thenReturn(broker);
        when(inRange.getFinalPremiumAmount()).thenReturn(AMOUNT_10);
        when(inRange.getCurrency()).thenReturn(null);
        when(policyRepository.findAll()).thenReturn(List.of(beforeStart, inRange));

        List<PolicyAggregateResponse> result = reportService.policiesByBroker(START_2026_01_10, null);

        assertEquals(EXPECTED_SIZE_1, result.size());
        assertEquals(BROKER_KEY_1, result.get(0).key());
        assertEquals(COUNT_1, result.get(0).count());
        assertEquals(0, AMOUNT_10.compareTo(result.get(0).totalValueInBase()));

        verify(policyRepository).findAll();
    }

    @Test
    void policiesByBrokerShouldSkipPoliciesAfterEndDate() {
        Broker broker = mock(Broker.class);
        when(broker.getId()).thenReturn(BROKER_ID_2);

        Policy afterEnd = mock(Policy.class);
        when(afterEnd.getEndDate()).thenReturn(DATE_2026_03_01);

        Policy inRange = mock(Policy.class);
        when(inRange.getEndDate()).thenReturn(END_2026_02_10);
        when(inRange.getBroker()).thenReturn(broker);
        when(inRange.getFinalPremiumAmount()).thenReturn(AMOUNT_5);
        when(inRange.getCurrency()).thenReturn(null);

        when(policyRepository.findAll()).thenReturn(List.of(afterEnd, inRange));

        List<PolicyAggregateResponse> result = reportService.policiesByBroker(null, END_2026_02_10);

        assertEquals(EXPECTED_SIZE_1, result.size());
        assertEquals(BROKER_KEY_2, result.get(0).key());
        assertEquals(COUNT_1, result.get(0).count());
        assertEquals(0, AMOUNT_5.compareTo(result.get(0).totalValueInBase()));

        verify(policyRepository).findAll();
    }

    @Test
    void policiesByBrokerShouldSkipWhenBrokerNullOrBrokerIdNull() {
        Policy brokerNull = mock(Policy.class);
        when(brokerNull.getStartDate()).thenReturn(DATE_2026_02_01);
        when(brokerNull.getEndDate()).thenReturn(DATE_2026_02_02);
        when(brokerNull.getBroker()).thenReturn(null);

        Broker brokerIdNull = mock(Broker.class);
        when(brokerIdNull.getId()).thenReturn(null);

        Policy idNull = mock(Policy.class);
        when(idNull.getStartDate()).thenReturn(DATE_2026_02_01);
        when(idNull.getEndDate()).thenReturn(DATE_2026_02_02);
        when(idNull.getBroker()).thenReturn(brokerIdNull);

        when(policyRepository.findAll()).thenReturn(List.of(brokerNull, idNull));

        List<PolicyAggregateResponse> result = reportService.policiesByBroker(START_2026_01_01, END_2026_12_31);

        assertTrue(result.isEmpty());

        verify(policyRepository).findAll();
    }

    @Test
    void policiesByBrokerShouldCoverFinalPremiumAndCurrencyRateBranches() {
        Broker b1 = mock(Broker.class);
        when(b1.getId()).thenReturn(BROKER_ID_1);

        Currency cRate2 = mock(Currency.class);
        when(cRate2.getExchangeRateToBase()).thenReturn(RATE_2);

        Currency cNullRate = mock(Currency.class);
        when(cNullRate.getExchangeRateToBase()).thenReturn(null);

        Policy p1 = mock(Policy.class);
        when(p1.getBroker()).thenReturn(b1);
        when(p1.getFinalPremiumAmount()).thenReturn(null);
        when(p1.getCurrency()).thenReturn(null);

        Policy p2 = mock(Policy.class);
        when(p2.getBroker()).thenReturn(b1);
        when(p2.getFinalPremiumAmount()).thenReturn(AMOUNT_10);
        when(p2.getCurrency()).thenReturn(cNullRate);

        Policy p3 = mock(Policy.class);
        when(p3.getBroker()).thenReturn(b1);
        when(p3.getFinalPremiumAmount()).thenReturn(AMOUNT_7);
        when(p3.getCurrency()).thenReturn(cRate2);

        when(policyRepository.findAll()).thenReturn(List.of(p1, p2, p3));

        List<PolicyAggregateResponse> result = reportService.policiesByBroker(null, null);

        assertEquals(EXPECTED_SIZE_1, result.size());
        assertEquals(BROKER_KEY_1, result.get(0).key());
        assertEquals(COUNT_3, result.get(0).count());

        assertEquals(0, TOTAL_24.compareTo(result.get(0).totalValueInBase()));

        verify(policyRepository).findAll();
    }

    @Test
    void policiesByBrokerShouldNotSkipWhenPolicyStartDateOrEndDateIsNull() {
        Broker broker = mock(Broker.class);
        when(broker.getId()).thenReturn(BROKER_ID_1);

        Policy startNull = mock(Policy.class);
        when(startNull.getStartDate()).thenReturn(null);
        when(startNull.getEndDate()).thenReturn(DATE_2026_02_01);
        when(startNull.getBroker()).thenReturn(broker);
        when(startNull.getFinalPremiumAmount()).thenReturn(AMOUNT_5);
        when(startNull.getCurrency()).thenReturn(null);

        Policy endNull = mock(Policy.class);
        when(endNull.getStartDate()).thenReturn(DATE_2026_02_01);
        when(endNull.getEndDate()).thenReturn(null);
        when(endNull.getBroker()).thenReturn(broker);
        when(endNull.getFinalPremiumAmount()).thenReturn(AMOUNT_7);
        when(endNull.getCurrency()).thenReturn(null);

        when(policyRepository.findAll()).thenReturn(List.of(startNull, endNull));

        List<PolicyAggregateResponse> result =
                reportService.policiesByBroker(START_2026_01_10, END_2026_12_31);

        assertEquals(EXPECTED_SIZE_1, result.size());
        assertEquals(BROKER_KEY_1, result.get(0).key());
        assertEquals(2L, result.get(0).count());
        assertEquals(0, AMOUNT_5.add(AMOUNT_7).compareTo(result.get(0).totalValueInBase()));

        verify(policyRepository).findAll();
    }

}
