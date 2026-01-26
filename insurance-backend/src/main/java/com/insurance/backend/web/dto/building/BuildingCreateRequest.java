package com.insurance.backend.web.dto.building;

import com.insurance.backend.domain.building.BuildingType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BuildingCreateRequest(
        @NotBlank @Size(max = 200) String address,
        @NotNull Long cityId,
        @NotNull @Min(1800) @Max(2100) Integer constructionYear,
        @NotNull BuildingType buildingType,
        @Min(0) @Max(300) Integer numberOfFloors,
        @NotNull @DecimalMin(value = "0.01") BigDecimal surfaceArea,
        @NotNull @DecimalMin(value = "0.01") BigDecimal insuredValue,
        boolean floodZone,
        boolean earthquakeRiskZone
) {}
