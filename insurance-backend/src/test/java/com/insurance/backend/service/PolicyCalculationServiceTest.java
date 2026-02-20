package com.insurance.backend.service;

import com.insurance.backend.service.calculation.PolicyCalculationContext;
import com.insurance.backend.service.calculation.PremiumAdjustmentStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PolicyCalculationServiceTest {

    private static final BigDecimal ZERO_SCALED = new BigDecimal("0.00");
    private static final BigDecimal BASE_PREMIUM_100 = new BigDecimal("100.00");

    private static final BigDecimal PCT_10 = new BigDecimal("0.10");
    private static final BigDecimal PCT_05 = new BigDecimal("0.05");

    private static final LocalDate POLICY_START_2026_01_10 = LocalDate.of(2026, 1, 10);

    @Test
    void calculateFinalPremiumBasePremiumNullReturnsZeroScaled() {
        PolicyCalculationService service = new PolicyCalculationService(List.of());

        BigDecimal result = service.calculateFinalPremium(null, LocalDate.now(), null, null);

        assertEquals(ZERO_SCALED, result);
    }

    @Test
    void calculateFinalPremiumSumsPercentagesFromStrategiesIgnoresNullsAndRoundsTo2Decimals() {

        PremiumAdjustmentStrategy returnsNullPct = new PremiumAdjustmentStrategy() {
            @Override
            public BigDecimal adjustmentPercentage(PolicyCalculationContext context) {
                return null;
            }

            @Override
            public int order() {
                return 1;
            }
        };

        PremiumAdjustmentStrategy pct10 = new PremiumAdjustmentStrategy() {
            @Override
            public BigDecimal adjustmentPercentage(PolicyCalculationContext context) {
                return PCT_10;
            }

            @Override
            public int order() {
                return 2;
            }
        };

        PremiumAdjustmentStrategy pct05 = new PremiumAdjustmentStrategy() {
            @Override
            public BigDecimal adjustmentPercentage(PolicyCalculationContext context) {
                return PCT_05;
            }

            @Override
            public int order() {
                return 3;
            }
        };

        List<PremiumAdjustmentStrategy> strategies = new ArrayList<>();
        strategies.add(null);
        strategies.add(returnsNullPct);
        strategies.add(pct05);
        strategies.add(pct10);

        PolicyCalculationService service = new PolicyCalculationService(strategies);

        BigDecimal result = service.calculateFinalPremium(
                BASE_PREMIUM_100,
                POLICY_START_2026_01_10,
                List.of(),
                List.of()
        );

        assertEquals(new BigDecimal("115.00"), result);
    }

    @Test
    void calculateFinalPremiumAppliesStrategiesInAscendingOrder() {
        List<Integer> callOrder = new ArrayList<>();

        PremiumAdjustmentStrategy second = new PremiumAdjustmentStrategy() {
            @Override
            public BigDecimal adjustmentPercentage(PolicyCalculationContext context) {
                callOrder.add(2);
                return BigDecimal.ZERO;
            }

            @Override
            public int order() {
                return 2;
            }
        };

        PremiumAdjustmentStrategy first = new PremiumAdjustmentStrategy() {
            @Override
            public BigDecimal adjustmentPercentage(PolicyCalculationContext context) {
                callOrder.add(1);
                return BigDecimal.ZERO;
            }

            @Override
            public int order() {
                return 1;
            }
        };

        PolicyCalculationService service = new PolicyCalculationService(List.of(second, first));

        service.calculateFinalPremium(BASE_PREMIUM_100, POLICY_START_2026_01_10, null, null);

        assertEquals(List.of(1, 2), callOrder);
    }
}