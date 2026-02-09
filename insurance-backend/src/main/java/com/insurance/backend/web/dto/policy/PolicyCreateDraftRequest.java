package com.insurance.backend.web.dto.policy;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PolicyCreateDraftRequest(
        @NotNull Long clientId,
        @NotNull Long buildingId,
        @NotNull Long brokerId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull @DecimalMin(value = "0.01") BigDecimal basePremiumAmount,
        @NotNull Long currencyId,
        List<Long> feeConfigurationIds
) {
}
