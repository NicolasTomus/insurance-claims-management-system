package com.insurance.backend.service;

import com.insurance.backend.domain.policy.Policy;
import com.insurance.backend.infrastructure.persistence.repository.policy.PolicyRepository;
import com.insurance.backend.web.dto.report.PolicyAggregateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final PolicyRepository policyRepository;

    public ReportService(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Transactional(readOnly = true)
    public List<PolicyAggregateResponse> policiesByBroker(LocalDate startDate, LocalDate endDate) {
        List<Policy> policies = policyRepository.findAll();

        Map<Long, Long> counts = new HashMap<>();
        Map<Long, BigDecimal> totals = new HashMap<>();

        for (Policy p : policies) {

            boolean shouldSkip =
                    (startDate != null && p.getStartDate() != null && p.getStartDate().isBefore(startDate))
                            || (endDate != null && p.getEndDate() != null && p.getEndDate().isAfter(endDate))
                            || (p.getBroker() == null || p.getBroker().getId() == null);

            if (shouldSkip) {
                continue;
            }

            Long brokerId = p.getBroker().getId();
            counts.put(brokerId, counts.getOrDefault(brokerId, 0L) + 1);

            BigDecimal finalPremium = p.getFinalPremiumAmount() != null ? p.getFinalPremiumAmount() : BigDecimal.ZERO;

            BigDecimal rate = BigDecimal.ONE;
            if (p.getCurrency() != null && p.getCurrency().getExchangeRateToBase() != null) {
                rate = p.getCurrency().getExchangeRateToBase();
            }

            BigDecimal valueInBase = finalPremium.multiply(rate);
            totals.put(brokerId, totals.getOrDefault(brokerId, BigDecimal.ZERO).add(valueInBase));
        }


        List<PolicyAggregateResponse> result = new ArrayList<>();
        for (Map.Entry<Long, Long> e : counts.entrySet()) {
            Long brokerId = e.getKey();
            result.add(new PolicyAggregateResponse(
                    brokerId.toString(),
                    e.getValue(),
                    totals.getOrDefault(brokerId, BigDecimal.ZERO)
            ));
        }

        result.sort((a, b) -> b.totalValueInBase().compareTo(a.totalValueInBase()));

        log.info("Report generated: policiesByBroker size={}", result.size());
        return result;
    }
}
