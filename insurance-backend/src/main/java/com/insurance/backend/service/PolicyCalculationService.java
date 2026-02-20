package com.insurance.backend.service;

import com.insurance.backend.domain.metadata.fee.FeeConfiguration;
import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;
import com.insurance.backend.service.calculation.PolicyCalculationContext;
import com.insurance.backend.service.calculation.PremiumAdjustmentStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class PolicyCalculationService {

    private final List<PremiumAdjustmentStrategy> strategies;

    public PolicyCalculationService(List<PremiumAdjustmentStrategy> strategies) {
        this.strategies = strategies;
    }

    public BigDecimal calculateFinalPremium(BigDecimal basePremium,
                                            LocalDate policyStartDate,
                                            List<FeeConfiguration> fees,
                                            List<RiskFactorConfiguration> risks) {

        if (basePremium == null) {
            return money(BigDecimal.ZERO);
        }

        PolicyCalculationContext context = new PolicyCalculationContext(policyStartDate, fees, risks);

        BigDecimal sumPct = strategies.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(PremiumAdjustmentStrategy::order))
                .map(s -> s.adjustmentPercentage(context))
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return money(basePremium.multiply(BigDecimal.ONE.add(sumPct)));
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
