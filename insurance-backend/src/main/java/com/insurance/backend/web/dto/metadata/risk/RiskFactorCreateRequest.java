package com.insurance.backend.web.dto.metadata.risk;

import com.insurance.backend.domain.metadata.risk.RiskLevel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RiskFactorCreateRequest(
        @NotNull RiskLevel level,
        Long referenceId,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal adjustmentPercentage,
        boolean active
) {}
