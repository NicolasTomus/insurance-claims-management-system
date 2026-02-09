package com.insurance.backend.web.dto.metadata.risk;

import com.insurance.backend.domain.metadata.risk.RiskLevel;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record RiskFactorUpdateRequest(
        RiskLevel level,
        Long referenceId,
        @DecimalMin(value = "0.0001") BigDecimal adjustmentPercentage,
        Boolean active
) {}
