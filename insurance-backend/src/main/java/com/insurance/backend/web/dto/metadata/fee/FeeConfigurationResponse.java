package com.insurance.backend.web.dto.metadata.fee;

import com.insurance.backend.domain.metadata.fee.FeeType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FeeConfigurationResponse(
        Long id,
        String name,
        FeeType type,
        BigDecimal percentage,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        boolean active
) {}
