package com.insurance.backend.web.dto.broker;

import com.insurance.backend.domain.broker.BrokerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record BrokerUpdateRequest(
        @Size(max = 200) String name,
        @Email @Size(max = 200) String email,
        @Size(max = 40) String phone,
        BrokerStatus status,
        BigDecimal commissionPercentage
) {
}
