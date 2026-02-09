package com.insurance.backend.web.dto.broker;

import com.insurance.backend.domain.broker.BrokerStatus;

import java.math.BigDecimal;

public record BrokerDetailsResponse(
        Long id,
        String brokerCode,
        String name,
        String email,
        String phone,
        BrokerStatus status,
        BigDecimal commissionPercentage
) {
}
