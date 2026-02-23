package com.insurance.backend.service.report;

import com.insurance.backend.infrastructure.persistence.repository.policy.PolicyRepository;
import com.insurance.backend.service.report.strategy.PoliciesByCityReportStrategy;
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
class PoliciesByCityReportStrategyTest {

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private PoliciesByCityReportStrategy strategy;

    @Test
    void typeShouldBeCity() {
        assertEquals(PolicyReportType.CITY, strategy.type());
    }

    @Test
    void generateShouldCallRepositoryAndMapToPolicyAggregateResponse() {
        PolicyReportFilter filter = mock(PolicyReportFilter.class);

        when(filter.from()).thenReturn(null);
        when(filter.to()).thenReturn(null);
        when(filter.status()).thenReturn(null);
        when(filter.currency()).thenReturn(null);
        when(filter.buildingType()).thenReturn(null);

        PolicyRepository.PolicyAggregateView view1 = mock(PolicyRepository.PolicyAggregateView.class);
        when(view1.getGroupId()).thenReturn(1L);
        when(view1.getGroupName()).thenReturn("Cluj");
        when(view1.getCurrencyCode()).thenReturn("EUR");
        when(view1.getPolicyCount()).thenReturn(3L);
        when(view1.getTotalFinalPremium()).thenReturn(new BigDecimal("123.45"));
        when(view1.getTotalFinalPremiumInBaseCurrency()).thenReturn(new BigDecimal("600.00"));

        PolicyRepository.PolicyAggregateView view2 = mock(PolicyRepository.PolicyAggregateView.class);
        when(view2.getGroupId()).thenReturn(2L);
        when(view2.getGroupName()).thenReturn("Bucuresti");
        when(view2.getCurrencyCode()).thenReturn("EUR");
        when(view2.getPolicyCount()).thenReturn(1L);
        when(view2.getTotalFinalPremium()).thenReturn(new BigDecimal("10.00"));
        when(view2.getTotalFinalPremiumInBaseCurrency()).thenReturn(new BigDecimal("50.00"));

        when(policyRepository.aggregateByCity(null, null, null, null, null))
                .thenReturn(List.of(view1, view2));

        List<PolicyAggregateResponse> result = strategy.generate(filter);

        assertNotNull(result);
        assertEquals(2, result.size());

        PolicyAggregateResponse r1 = result.get(0);
        assertEquals(1L, r1.groupId());
        assertEquals("Cluj", r1.groupName());
        assertEquals("EUR", r1.currencyCode());
        assertEquals(3L, r1.policyCount());
        assertEquals(new BigDecimal("123.45"), r1.totalFinalPremium());
        assertEquals(new BigDecimal("600.00"), r1.totalFinalPremiumInBaseCurrency());

        PolicyAggregateResponse r2 = result.get(1);
        assertEquals(2L, r2.groupId());
        assertEquals("Bucuresti", r2.groupName());

        verify(policyRepository, times(1))
                .aggregateByCity(null, null, null, null, null);
    }
}