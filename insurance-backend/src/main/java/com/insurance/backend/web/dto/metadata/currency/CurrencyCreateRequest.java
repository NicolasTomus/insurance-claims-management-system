package com.insurance.backend.web.dto.metadata.currency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CurrencyCreateRequest(
        @NotBlank
        @Size(min = 3, max = 3)
        String code,
        @NotBlank String name,
        @NotNull BigDecimal exchangeRateToBase,
        boolean active
) {
    public CurrencyCreateRequest {
        code = (code == null) ? null : code.trim().toUpperCase();
        name = (name == null) ? null : name.trim();
    }
}
