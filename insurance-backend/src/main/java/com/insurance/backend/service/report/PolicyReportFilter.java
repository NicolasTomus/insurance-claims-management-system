package com.insurance.backend.service.report;

import com.insurance.backend.domain.building.BuildingType;
import com.insurance.backend.domain.policy.PolicyStatus;

import java.time.LocalDate;

public record PolicyReportFilter(
        LocalDate from,
        LocalDate to,
        PolicyStatus status,
        String currency,
        BuildingType buildingType
) {}