package com.insurance.backend.service;

import com.insurance.backend.domain.metadata.fee.FeeConfiguration;
import com.insurance.backend.domain.metadata.fee.FeeType;
import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;
import com.insurance.backend.domain.metadata.risk.RiskLevel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PolicyCalculationServiceTest {

    private static final BigDecimal ZERO_SCALED = new BigDecimal("0.00");

    private static final BigDecimal BASE_PREMIUM_100 = new BigDecimal("100.00");
    private static final BigDecimal PREMIUM_100 = new BigDecimal("100.00");
    private static final BigDecimal PREMIUM_110 = new BigDecimal("110.00");
    private static final BigDecimal PREMIUM_115 = new BigDecimal("115.00");

    private static final BigDecimal PCT_10 = new BigDecimal("0.10");
    private static final BigDecimal PCT_05 = new BigDecimal("0.05");

    private static final long RISK_FACTOR_ID = 1L;

    private static final LocalDate POLICY_START_2026_01_10 = LocalDate.of(2026, 1, 10);
    private static final LocalDate DATE_2026_01_01 = LocalDate.of(2026, 1, 1);
    private static final LocalDate DATE_2026_02_01 = LocalDate.of(2026, 2, 1);
    private static final LocalDate DATE_2099_01_01 = LocalDate.of(2099, 1, 1);
    private static final LocalDate DATE_2099_12_31 = LocalDate.of(2099, 12, 31);

    private static final FeeType FEE_TYPE_ADMIN = FeeType.ADMIN_FEE;
    private static final RiskLevel RISK_LEVEL_CITY = RiskLevel.CITY;

    private final PolicyCalculationService service = new PolicyCalculationService();

    @Test
    void calculateFinalPremiumBasePremiumNullReturnsZeroScaled() {
        BigDecimal result = service.calculateFinalPremium(null, LocalDate.now(), null, null);
        assertEquals(ZERO_SCALED, result);
    }

    @Test
    void calculateFinalPremiumCoversFeesAndRisksAllBranchesAndAddsPercentages() {
        FeeConfiguration inactiveFee = new FeeConfiguration(
                "Inactive", FEE_TYPE_ADMIN, PCT_10,
                null, null, false
        );

        FeeConfiguration notEffectiveFrom = new FeeConfiguration(
                "FromInFuture", FEE_TYPE_ADMIN, PCT_10,
                DATE_2026_02_01, null, true
        );

        FeeConfiguration effectivePctNull = new FeeConfiguration(
                "PctNull", FEE_TYPE_ADMIN, null,
                null, null, true
        );

        FeeConfiguration effectivePct10 = new FeeConfiguration(
                "Pct10", FEE_TYPE_ADMIN, PCT_10,
                null, null, true
        );

        RiskFactorConfiguration inactiveRisk = new RiskFactorConfiguration(
                RISK_LEVEL_CITY, RISK_FACTOR_ID, PCT_05, false
        );

        RiskFactorConfiguration riskPctNull = new RiskFactorConfiguration(
                RISK_LEVEL_CITY, RISK_FACTOR_ID, null, true
        );

        RiskFactorConfiguration riskPct05 = new RiskFactorConfiguration(
                RISK_LEVEL_CITY, RISK_FACTOR_ID, PCT_05, true
        );

        List<FeeConfiguration> fees = new ArrayList<>();
        fees.add(null);
        fees.add(inactiveFee);
        fees.add(notEffectiveFrom);
        fees.add(effectivePctNull);
        fees.add(effectivePct10);

        List<RiskFactorConfiguration> risks = new ArrayList<>();
        risks.add(null);
        risks.add(inactiveRisk);
        risks.add(riskPctNull);
        risks.add(riskPct05);

        BigDecimal result = service.calculateFinalPremium(BASE_PREMIUM_100, POLICY_START_2026_01_10, fees, risks);

        assertEquals(PREMIUM_115, result);
    }

    @Test
    void calculateFinalPremiumPolicyStartNullMakesIsEffectiveForDateReturnTrueAndIncludesFee() {
        FeeConfiguration feeFromFutureButDateNull = new FeeConfiguration(
                "FromFutureButDateNull", FEE_TYPE_ADMIN, PCT_10,
                DATE_2099_01_01, DATE_2099_12_31, true
        );

        BigDecimal result = service.calculateFinalPremium(
                PREMIUM_100,
                null,
                List.of(feeFromFutureButDateNull),
                null
        );

        assertEquals(PREMIUM_110, result);
    }

    @Test
    void calculateFinalPremiumFeeToBeforePolicyStartTriggersToDateAfterBranchAndSkipsFee() {
        FeeConfiguration expiredFee = new FeeConfiguration(
                "Expired", FEE_TYPE_ADMIN, PCT_10,
                null, DATE_2026_01_01, true
        );

        BigDecimal result = service.calculateFinalPremium(
                PREMIUM_100,
                POLICY_START_2026_01_10,
                List.of(expiredFee),
                null
        );

        assertEquals(PREMIUM_100, result);
    }

    @Test
    void calculateFinalPremiumFeesEmptyReturnsZeroFeePct() {
        BigDecimal result = service.calculateFinalPremium(
                BASE_PREMIUM_100,
                POLICY_START_2026_01_10,
                List.of(),
                null
        );

        assertEquals(PREMIUM_100, result);
    }

}
