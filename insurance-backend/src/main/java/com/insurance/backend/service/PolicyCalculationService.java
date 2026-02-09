package com.insurance.backend.service;

import com.insurance.backend.domain.metadata.fee.FeeConfiguration;
import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class PolicyCalculationService {

    public BigDecimal calculateFinalPremium(BigDecimal basePremium,
                                            LocalDate policyStartDate,
                                            List<FeeConfiguration> fees,
                                            List<RiskFactorConfiguration> risks) {

        if (basePremium == null) {
            return money(BigDecimal.ZERO);
        }

        BigDecimal sumPct = sumFeePercentages(fees, policyStartDate)
                .add(sumRiskPercentages(risks));

        return money(basePremium.multiply(BigDecimal.ONE.add(sumPct)));
    }

    private BigDecimal sumFeePercentages(List<FeeConfiguration> fees, LocalDate policyStartDate) {
        if (fees == null || fees.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return fees.stream()
                .filter(this::isApplicableFee)
                .filter(f -> isEffectiveForDate(f.getEffectiveFrom(), f.getEffectiveTo(), policyStartDate))
                .map(FeeConfiguration::getPercentage)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isApplicableFee(FeeConfiguration fee) {
        return fee != null && fee.isActive();
    }

    private BigDecimal sumRiskPercentages(List<RiskFactorConfiguration> risks) {
        if (risks == null || risks.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return risks.stream()
                .filter(this::isApplicableRisk)
                .map(RiskFactorConfiguration::getAdjustmentPercentage)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isApplicableRisk(RiskFactorConfiguration risk) {
        return risk != null && risk.isActive();
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }


    private boolean isEffectiveForDate(LocalDate from, LocalDate to, LocalDate date) {
        if (date == null) return true;

        boolean afterFrom = (from == null) || !date.isBefore(from);
        boolean beforeTo  = (to == null)   || !date.isAfter(to);

        return afterFrom && beforeTo;
    }
}
