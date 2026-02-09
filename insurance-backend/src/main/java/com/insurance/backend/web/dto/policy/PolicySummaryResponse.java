package com.insurance.backend.web.dto.policy;

import com.insurance.backend.domain.policy.PolicyStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicySummaryResponse(
        Long id,
        String policyNumber,
        PolicyStatus status,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal finalPremiumAmount,
        String currencyCode
) {
}
