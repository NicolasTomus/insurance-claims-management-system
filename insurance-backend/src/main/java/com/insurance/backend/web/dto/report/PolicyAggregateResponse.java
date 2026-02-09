package com.insurance.backend.web.dto.report;

import java.math.BigDecimal;

public record PolicyAggregateResponse(
        String key,
        long count,
        BigDecimal totalValueInBase
) {}
