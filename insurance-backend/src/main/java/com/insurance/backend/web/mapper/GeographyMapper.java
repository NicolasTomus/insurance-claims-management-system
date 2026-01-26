package com.insurance.backend.web.mapper;

import com.insurance.backend.domain.geography.City;
import com.insurance.backend.domain.geography.County;
import com.insurance.backend.domain.geography.Country;
import com.insurance.backend.web.dto.geography.CityResponse;
import com.insurance.backend.web.dto.geography.CountyResponse;
import com.insurance.backend.web.dto.geography.CountryResponse;
import org.springframework.stereotype.Component;

@Component
public class GeographyMapper {

    public CountryResponse toCountry(Country c) {
        return new CountryResponse(c.getId(), c.getName());
    }

    public CountyResponse toCounty(County c) {
        return new CountyResponse(c.getId(), c.getName(), c.getCountry().getId());
    }

    public CityResponse toCity(City c) {
        return new CityResponse(c.getId(), c.getName(), c.getCounty().getId());
    }
}
