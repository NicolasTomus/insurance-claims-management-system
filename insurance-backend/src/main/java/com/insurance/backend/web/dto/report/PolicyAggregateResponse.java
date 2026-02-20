package com.insurance.backend.web.dto.report;

import java.math.BigDecimal;

public record PolicyAggregateResponse(
        Long groupId,
        String groupName,
        String currencyCode,
        long policyCount,
        BigDecimal totalFinalPremium,
        BigDecimal totalFinalPremiumInBaseCurrency
) {}