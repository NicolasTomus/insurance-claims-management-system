package com.insurance.backend.web.dto.broker;

import com.insurance.backend.domain.broker.BrokerStatus;

public record BrokerSummaryResponse(
        Long id,
        String brokerCode,
        String name,
        BrokerStatus status
) {
}
