package com.insurance.backend.web.dto.metadata.risk;

import com.insurance.backend.domain.metadata.risk.RiskLevel;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RiskFactorCreateRequest(
        @NotNull RiskLevel level,
        Long referenceId,
        @NotNull
        @DecimalMin(value = "-1.0000")
        @DecimalMax(value = "10.0000")
        BigDecimal adjustmentPercentage,
        boolean active
) {}
