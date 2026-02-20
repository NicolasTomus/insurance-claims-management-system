package com.insurance.backend.service.calculation;

import com.insurance.backend.domain.metadata.fee.FeeConfiguration;
import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;

import java.time.LocalDate;
import java.util.List;

public record PolicyCalculationContext(
        LocalDate policyStartDate,
        List<FeeConfiguration> fees,
        List<RiskFactorConfiguration> risks
) {}
