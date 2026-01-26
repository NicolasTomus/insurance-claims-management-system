package com.insurance.backend.web.dto.building;

import com.insurance.backend.domain.building.BuildingType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BuildingUpdateRequest(

        Long clientId,
        @Size(max = 200) String address,
        Long cityId,
        @Min(1800) @Max(2100) Integer constructionYear,
        BuildingType buildingType,
        @Min(0) @Max(300) Integer numberOfFloors,
        @DecimalMin(value = "0.01") BigDecimal surfaceArea,
        @DecimalMin(value = "0.01") BigDecimal insuredValue,
        Boolean floodZone,
        Boolean earthquakeRiskZone
) {}
