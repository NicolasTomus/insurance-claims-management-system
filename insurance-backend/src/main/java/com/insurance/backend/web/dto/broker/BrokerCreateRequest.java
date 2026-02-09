package com.insurance.backend.web.dto.broker;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record BrokerCreateRequest(
        @NotBlank @Size(max = 30) String brokerCode,
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Email @Size(max = 200) String email,
        @NotBlank @Size(max = 40) String phone,
        BigDecimal commissionPercentage
) {
}
