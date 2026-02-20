package com.insurance.backend.service.calculation.risk;

import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;
import com.insurance.backend.service.calculation.PolicyCalculationContext;
import com.insurance.backend.service.calculation.PremiumAdjustmentStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Stream;

@Component
public class RiskFactorAdjustmentStrategy implements PremiumAdjustmentStrategy {

    @Override
    public BigDecimal adjustmentPercentage(PolicyCalculationContext context) {
        return sumPercentages(
                risksFrom(context)
                        .filter(this::isApplicableRisk)
                        .map(RiskFactorConfiguration::getAdjustmentPercentage)
                        .filter(Objects::nonNull)
        );
    }

    private Stream<RiskFactorConfiguration> risksFrom(PolicyCalculationContext context) {
        if (context == null || context.risks() == null || context.risks().isEmpty()) {
            return Stream.empty();
        }
        return context.risks().stream().filter(Objects::nonNull);
    }

    private boolean isApplicableRisk(RiskFactorConfiguration risk) {
        return risk.isActive();
    }

    private BigDecimal sumPercentages(Stream<BigDecimal> percentages) {
        return percentages.reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public int order() {
        return 40;
    }
}
