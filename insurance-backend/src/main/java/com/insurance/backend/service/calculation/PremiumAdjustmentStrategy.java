package com.insurance.backend.service.calculation;

import java.math.BigDecimal;

public interface PremiumAdjustmentStrategy {

    BigDecimal adjustmentPercentage(PolicyCalculationContext context);

    int order();
}
