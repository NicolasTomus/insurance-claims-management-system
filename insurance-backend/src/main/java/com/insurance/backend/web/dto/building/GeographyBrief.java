package com.insurance.backend.web.dto.building;

public record GeographyBrief(
        Long countryId, String countryName,
        Long countyId, String countyName,
        Long cityId, String cityName
) {}
