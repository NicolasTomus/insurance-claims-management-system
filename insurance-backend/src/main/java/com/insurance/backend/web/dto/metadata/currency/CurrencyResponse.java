package com.insurance.backend.web.dto.metadata.currency;

import java.math.BigDecimal;

public record CurrencyResponse(
        Long id,
        String code,
        String name,
        BigDecimal exchangeRateToBase,
        boolean active
) {}
