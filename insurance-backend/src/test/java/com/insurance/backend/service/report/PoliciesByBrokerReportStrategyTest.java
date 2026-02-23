package com.insurance.backend.service.report;

import com.insurance.backend.infrastructure.persistence.repository.policy.PolicyRepository;
import com.insurance.backend.service.report.strategy.PoliciesByBrokerReportStrategy;
import com.insurance.backend.web.dto.report.PolicyAggregateResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PoliciesByBrokerReportStrategyTest {

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private PoliciesByBrokerReportStrategy strategy;

    @Test
    void typeShouldBeBroker() {
        assertEquals(PolicyReportType.BROKER, strategy.type());
    }

    @Test
    void generateShouldCallRepositoryAndMapToPolicyAggregateResponse() {
        // given
        PolicyReportFilter filter = mock(PolicyReportFilter.class);
        when(filter.from()).thenReturn(null);
        when(filter.to()).thenReturn(null);
        when(filter.status()).thenReturn(null);
        when(filter.currency()).thenReturn(null);
        when(filter.buildingType()).thenReturn(null);

        PolicyRepository.PolicyAggregateView view1 = mock(PolicyRepository.PolicyAggregateView.class);
        when(view1.getGroupId()).thenReturn(99L);
        when(view1.getGroupName()).thenReturn("Broker One");
        when(view1.getCurrencyCode()).thenReturn("EUR");
        when(view1.getPolicyCount()).thenReturn(5L);
        when(view1.getTotalFinalPremium()).thenReturn(new BigDecimal("999.99"));
        when(view1.getTotalFinalPremiumInBaseCurrency()).thenReturn(new BigDecimal("1200.00"));

        when(policyRepository.aggregateByBroker(null, null, null, null, null))
                .thenReturn(List.of(view1));

        // when
        List<PolicyAggregateResponse> result = strategy.generate(filter);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());

        PolicyAggregateResponse r = result.get(0);
        assertEquals(99L, r.groupId());
        assertEquals("Broker One", r.groupName());
        assertEquals("EUR", r.currencyCode());
        assertEquals(5L, r.policyCount());
        assertEquals(new BigDecimal("999.99"), r.totalFinalPremium());
        assertEquals(new BigDecimal("1200.00"), r.totalFinalPremiumInBaseCurrency());

        verify(policyRepository, times(1))
                .aggregateByBroker(null, null, null, null, null);
    }
}