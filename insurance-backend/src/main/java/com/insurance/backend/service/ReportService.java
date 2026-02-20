package com.insurance.backend.service;

import com.insurance.backend.service.report.*;
import com.insurance.backend.web.dto.report.PolicyAggregateResponse;
import com.insurance.backend.web.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final Map<PolicyReportType, PolicyReportStrategy> strategies;

    public ReportService(List<PolicyReportStrategy> strategies) {
        this.strategies = new EnumMap<>(PolicyReportType.class);
        for (PolicyReportStrategy s : strategies) {
            this.strategies.put(s.type(), s);
        }
    }

    @Transactional(readOnly = true)
    public List<PolicyAggregateResponse> policyReport(PolicyReportType type, PolicyReportFilter filter) {

        String normalizedCurrency = (filter.currency() == null || filter.currency().isBlank())
                ? null : filter.currency().trim().toUpperCase();

        PolicyReportFilter normalizedFilter = new PolicyReportFilter(
                filter.from(),
                filter.to(),
                filter.status(),
                normalizedCurrency,
                filter.buildingType()
        );

        validate(normalizedFilter);

        PolicyReportStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalStateException("No strategy registered for " + type);
        }

        return strategy.generate(normalizedFilter);
    }

    private void validate(PolicyReportFilter filter) {
        if (filter == null) {
            throw new BadRequestException("Filter is required");
        }
        if (filter.from() != null && filter.to() != null && filter.from().isAfter(filter.to())) {
            throw new BadRequestException("from must be <= to");
        }
    }
}