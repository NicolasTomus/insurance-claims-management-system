package com.insurance.backend.web.dto.policy;

import com.insurance.backend.domain.policy.PolicyStatus;
import com.insurance.backend.web.dto.broker.BrokerSummaryResponse;
import com.insurance.backend.web.dto.building.BuildingDetailsResponse;
import com.insurance.backend.web.dto.client.ClientSummaryResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyDetailsResponse(
        Long id,
        String policyNumber,
        PolicyStatus status,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal basePremiumAmount,
        BigDecimal finalPremiumAmount,
        String currencyCode,
        ClientSummaryResponse client,
        BuildingDetailsResponse building,
        BrokerSummaryResponse broker
) {
}
