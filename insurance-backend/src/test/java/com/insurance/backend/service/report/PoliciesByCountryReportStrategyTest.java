package com.insurance.backend.service.report;

import com.insurance.backend.infrastructure.persistence.repository.policy.PolicyRepository;
import com.insurance.backend.service.report.strategy.PoliciesByCountryReportStrategy;
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
class PoliciesByCountryReportStrategyTest {

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private PoliciesByCountryReportStrategy strategy;

    @Test
    void typeShouldBeCountry() {
        assertEquals(PolicyReportType.COUNTRY, strategy.type());
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
        when(view1.getGroupId()).thenReturn(1L);
        when(view1.getGroupName()).thenReturn("Romania");
        when(view1.getCurrencyCode()).thenReturn("EUR");
        when(view1.getPolicyCount()).thenReturn(3L);
        when(view1.getTotalFinalPremium()).thenReturn(new BigDecimal("123.45"));
        when(view1.getTotalFinalPremiumInBaseCurrency()).thenReturn(new BigDecimal("600.00"));

        when(policyRepository.aggregateByCountry(null, null, null, null, null))
                .thenReturn(List.of(view1));

        // when
        List<PolicyAggregateResponse> result = strategy.generate(filter);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());

        PolicyAggregateResponse r = result.get(0);
        assertEquals(1L, r.groupId());
        assertEquals("Romania", r.groupName());
        assertEquals("EUR", r.currencyCode());
        assertEquals(3L, r.policyCount());
        assertEquals(new BigDecimal("123.45"), r.totalFinalPremium());
        assertEquals(new BigDecimal("600.00"), r.totalFinalPremiumInBaseCurrency());

        verify(policyRepository, times(1))
                .aggregateByCountry(null, null, null, null, null);
    }
}