package com.insurance.backend.service.report.strategy;

import com.insurance.backend.infrastructure.persistence.repository.policy.PolicyRepository;
import com.insurance.backend.service.report.*;
import com.insurance.backend.web.dto.report.PolicyAggregateResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PoliciesByCountryReportStrategy implements PolicyReportStrategy {

    private final PolicyRepository policyRepository;

    public PoliciesByCountryReportStrategy(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Override
    public PolicyReportType type() {
        return PolicyReportType.COUNTRY;
    }

    @Override
    public List<PolicyAggregateResponse> generate(PolicyReportFilter filter) {
        return policyRepository.aggregateByCountry(
                filter.from(),
                filter.to(),
                filter.status(),
                filter.currency(),
                filter.buildingType()
        ).stream().map(this::map).toList();
    }

    private PolicyAggregateResponse map(PolicyRepository.PolicyAggregateView v) {
        return new PolicyAggregateResponse(
                v.getGroupId(),
                v.getGroupName(),
                v.getCurrencyCode(),
                v.getPolicyCount(),
                v.getTotalFinalPremium(),
                v.getTotalFinalPremiumInBaseCurrency()
        );
    }
}