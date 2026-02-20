package com.insurance.backend.service.calculation.fee;

import com.insurance.backend.domain.metadata.fee.FeeConfiguration;
import com.insurance.backend.domain.metadata.fee.FeeType;
import com.insurance.backend.service.calculation.PolicyCalculationContext;
import com.insurance.backend.service.calculation.PremiumAdjustmentStrategy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class AbstractFeePercentageStrategy implements PremiumAdjustmentStrategy {

    protected abstract FeeType supportedType();

    @Override
    public BigDecimal adjustmentPercentage(PolicyCalculationContext context) {
        LocalDate startDate = (context == null) ? null : context.policyStartDate();

        return sumPercentages(
                feesFrom(context)
                        .filter(fee -> isApplicable(fee, startDate))
                        .map(FeeConfiguration::getPercentage)
                        .filter(Objects::nonNull)
        );
    }

    private Stream<FeeConfiguration> feesFrom(PolicyCalculationContext context) {
        if (context == null || context.fees() == null || context.fees().isEmpty()) {
            return Stream.empty();
        }
        return context.fees().stream().filter(Objects::nonNull);
    }

    private boolean isApplicable(FeeConfiguration fee, LocalDate policyStartDate) {
        return fee.isActive()
                && fee.getType() == supportedType()
                && isEffectiveForDate(fee.getEffectiveFrom(), fee.getEffectiveTo(), policyStartDate);
    }

    private BigDecimal sumPercentages(Stream<BigDecimal> percentages) {
        return percentages.reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    protected boolean isEffectiveForDate(LocalDate from, LocalDate to, LocalDate date) {
        if (date == null) return true;
        return isAfterOrEqual(date, from) && isBeforeOrEqual(date, to);
    }

    private boolean isAfterOrEqual(LocalDate date, LocalDate from) {
        if (from == null) return true;
        return !date.isBefore(from);
    }

    private boolean isBeforeOrEqual(LocalDate date, LocalDate to) {
        if (to == null) return true;
        return !date.isAfter(to);
    }
}
