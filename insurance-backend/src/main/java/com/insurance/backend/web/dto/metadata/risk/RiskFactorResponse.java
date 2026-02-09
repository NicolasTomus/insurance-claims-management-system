package com.insurance.backend.web.dto.metadata.risk;

import com.insurance.backend.domain.metadata.risk.RiskLevel;

import java.math.BigDecimal;

public record RiskFactorResponse(
        Long id,
        RiskLevel level,
        Long referenceId,
        BigDecimal adjustmentPercentage,
        boolean active
) {}
