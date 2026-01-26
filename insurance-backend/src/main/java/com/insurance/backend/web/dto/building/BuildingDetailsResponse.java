package com.insurance.backend.web.dto.building;

import com.insurance.backend.domain.building.BuildingType;

import java.math.BigDecimal;

public record BuildingDetailsResponse(
        Long id,
        Long clientId,
        String address,
        Integer constructionYear,
        BuildingType buildingType,
        Integer numberOfFloors,
        BigDecimal surfaceArea,
        BigDecimal insuredValue,
        boolean floodZone,
        boolean earthquakeRiskZone,
        GeographyBrief geography
) {}
