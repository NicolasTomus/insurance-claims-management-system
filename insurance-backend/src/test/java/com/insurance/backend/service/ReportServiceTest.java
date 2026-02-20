package com.insurance.backend.service;

import com.insurance.backend.service.report.PolicyReportFilter;
import com.insurance.backend.service.report.PolicyReportStrategy;
import com.insurance.backend.service.report.PolicyReportType;
import com.insurance.backend.web.dto.report.PolicyAggregateResponse;
import com.insurance.backend.web.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private PolicyReportStrategy countryStrategy;

    @Mock
    private PolicyReportStrategy cityStrategy;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        when(countryStrategy.type()).thenReturn(PolicyReportType.COUNTRY);
        when(cityStrategy.type()).thenReturn(PolicyReportType.CITY);

        reportService = new ReportService(List.of(countryStrategy, cityStrategy));

        clearInvocations(countryStrategy, cityStrategy);
    }

    @Test
    void policyReportShouldCallMatchingStrategyAndNormalizeCurrency() {
        PolicyReportFilter filter = new PolicyReportFilter(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                null,
                " eur ",
                null
        );

        List<PolicyAggregateResponse> expected = List.of(mock(PolicyAggregateResponse.class));
        when(cityStrategy.generate(any(PolicyReportFilter.class))).thenReturn(expected);

        List<PolicyAggregateResponse> result = reportService.policyReport(PolicyReportType.CITY, filter);

        assertSame(expected, result);

        ArgumentCaptor<PolicyReportFilter> captor = ArgumentCaptor.forClass(PolicyReportFilter.class);
        verify(cityStrategy).generate(captor.capture());

        PolicyReportFilter normalized = captor.getValue();
        assertEquals("EUR", normalized.currency());
        assertEquals(filter.from(), normalized.from());
        assertEquals(filter.to(), normalized.to());
        assertEquals(filter.status(), normalized.status());
        assertEquals(filter.buildingType(), normalized.buildingType());

        verifyNoInteractions(countryStrategy);
        verifyNoMoreInteractions(cityStrategy);
    }

    @Test
    void policyReportShouldNormalizeCurrencyToNullWhenBlank() {
        PolicyReportFilter filter = new PolicyReportFilter(
                null, null, null, "   ", null
        );

        when(countryStrategy.generate(any(PolicyReportFilter.class)))
                .thenReturn(Collections.emptyList());

        reportService.policyReport(PolicyReportType.COUNTRY, filter);

        ArgumentCaptor<PolicyReportFilter> captor = ArgumentCaptor.forClass(PolicyReportFilter.class);
        verify(countryStrategy).generate(captor.capture());
        assertNull(captor.getValue().currency());

        verifyNoInteractions(cityStrategy);
        verifyNoMoreInteractions(countryStrategy);
    }

    @Test
    void policyReportShouldNormalizeCurrencyToNullWhenNull() {
        PolicyReportFilter filter = new PolicyReportFilter(
                null, null, null, null, null
        );

        when(countryStrategy.generate(any(PolicyReportFilter.class)))
                .thenReturn(Collections.emptyList());

        reportService.policyReport(PolicyReportType.COUNTRY, filter);

        ArgumentCaptor<PolicyReportFilter> captor = ArgumentCaptor.forClass(PolicyReportFilter.class);
        verify(countryStrategy).generate(captor.capture());
        assertNull(captor.getValue().currency());

        verifyNoInteractions(cityStrategy);
        verifyNoMoreInteractions(countryStrategy);
    }

    @Test
    void policyReportShouldThrowBadRequestWhenFromAfterTo() {
        PolicyReportFilter filter = new PolicyReportFilter(
                LocalDate.of(2025, 12, 31),
                LocalDate.of(2025, 1, 1),
                null,
                "EUR",
                null
        );

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> reportService.policyReport(PolicyReportType.COUNTRY, filter)
        );
        assertEquals("from must be <= to", ex.getMessage());

        verifyNoInteractions(countryStrategy);
        verifyNoInteractions(cityStrategy);
    }

    @Test
    void policyReportShouldThrowWhenNoStrategyRegisteredForType() {
        ReportService service = new ReportService(List.of());
        PolicyReportFilter filter = new PolicyReportFilter(
                null, null, null, "EUR", null
        );

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.policyReport(PolicyReportType.BROKER, filter)
        );

        assertTrue(ex.getMessage().contains("No strategy registered for"));
    }
}