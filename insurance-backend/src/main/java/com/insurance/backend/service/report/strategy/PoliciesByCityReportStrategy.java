package com.insurance.backend.service.report.strategy;

import com.insurance.backend.infrastructure.persistence.repository.policy.PolicyRepository;
import com.insurance.backend.service.report.PolicyReportFilter;
import com.insurance.backend.service.report.PolicyReportStrategy;
import com.insurance.backend.service.report.PolicyReportType;
import com.insurance.backend.web.dto.report.PolicyAggregateResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PoliciesByCityReportStrategy implements PolicyReportStrategy {

    private final PolicyRepository policyRepository;

    public PoliciesByCityReportStrategy(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Override
    public PolicyReportType type() {
        return PolicyReportType.CITY;
    }

    @Override
    public List<PolicyAggregateResponse> generate(PolicyReportFilter filter) {
        return policyRepository.aggregateByCity(
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