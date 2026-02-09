package com.insurance.backend.web.dto.metadata.currency;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CurrencyUpdateRequest(
        @Size(max = 100) String name,
        @DecimalMin(value = "0.00000001") BigDecimal exchangeRateToBase,
        Boolean active
) {}
