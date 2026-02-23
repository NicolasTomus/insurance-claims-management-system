package com.insurance.backend.service.report;

import com.insurance.backend.infrastructure.persistence.repository.policy.PolicyRepository;
import com.insurance.backend.service.report.strategy.PoliciesByCountyReportStrategy;
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
class PoliciesByCountyReportStrategyTest {

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private PoliciesByCountyReportStrategy strategy;

    @Test
    void typeShouldBeCounty() {
        assertEquals(PolicyReportType.COUNTY, strategy.type());
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
        when(view1.getGroupId()).thenReturn(11L);
        when(view1.getGroupName()).thenReturn("Cluj");
        when(view1.getCurrencyCode()).thenReturn("EUR");
        when(view1.getPolicyCount()).thenReturn(2L);
        when(view1.getTotalFinalPremium()).thenReturn(new BigDecimal("50.00"));
        when(view1.getTotalFinalPremiumInBaseCurrency()).thenReturn(new BigDecimal("200.00"));

        when(policyRepository.aggregateByCounty(null, null, null, null, null))
                .thenReturn(List.of(view1));

        // when
        List<PolicyAggregateResponse> result = strategy.generate(filter);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());

        PolicyAggregateResponse r = result.get(0);
        assertEquals(11L, r.groupId());
        assertEquals("Cluj", r.groupName());
        assertEquals("EUR", r.currencyCode());
        assertEquals(2L, r.policyCount());
        assertEquals(new BigDecimal("50.00"), r.totalFinalPremium());
        assertEquals(new BigDecimal("200.00"), r.totalFinalPremiumInBaseCurrency());

        verify(policyRepository, times(1))
                .aggregateByCounty(null, null, null, null, null);
    }
}