package com.insurance.backend.web.dto.metadata.fee;

import com.insurance.backend.domain.metadata.fee.FeeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FeeConfigurationUpdateRequest(
        @Size(max = 200) String name,
        FeeType type,
        @DecimalMin(value = "0.0001") BigDecimal percentage,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        Boolean active
) {}
