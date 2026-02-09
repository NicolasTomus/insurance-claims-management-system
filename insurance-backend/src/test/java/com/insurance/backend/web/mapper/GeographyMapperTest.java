package com.insurance.backend.web.mapper;

import com.insurance.backend.domain.geography.City;
import com.insurance.backend.domain.geography.County;
import com.insurance.backend.domain.geography.Country;
import com.insurance.backend.web.dto.geography.CityResponse;
import com.insurance.backend.web.dto.geography.CountyResponse;
import com.insurance.backend.web.dto.geography.CountryResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GeographyMapperTest {

    private static final long COUNTRY_ID = 1L;
    private static final long COUNTRY_ID_REF = 10L;

    private static final long COUNTY_ID = 2L;
    private static final long COUNTY_ID_REF = 20L;

    private static final long CITY_ID = 3L;

    private static final String COUNTRY_NAME = "Romania";
    private static final String COUNTY_NAME = "Cluj";
    private static final String CITY_NAME = "Cluj-Napoca";

    private final GeographyMapper geographyMapper = new GeographyMapper();

    @Test
    void toCountryShouldMapFields() {
        Country country = mock(Country.class);
        when(country.getId()).thenReturn(COUNTRY_ID);
        when(country.getName()).thenReturn(COUNTRY_NAME);

        CountryResponse result = geographyMapper.toCountry(country);

        assertNotNull(result);
        assertEquals(COUNTRY_ID, result.id());
        assertEquals(COUNTRY_NAME, result.name());
    }

    @Test
    void toCountyShouldMapFieldsAndCountryId() {
        Country country = mock(Country.class);
        when(country.getId()).thenReturn(COUNTRY_ID_REF);

        County county = mock(County.class);
        when(county.getId()).thenReturn(COUNTY_ID);
        when(county.getName()).thenReturn(COUNTY_NAME);
        when(county.getCountry()).thenReturn(country);

        CountyResponse result = geographyMapper.toCounty(county);

        assertNotNull(result);
        assertEquals(COUNTY_ID, result.id());
        assertEquals(COUNTY_NAME, result.name());
        assertEquals(COUNTRY_ID_REF, result.countryId());
    }

    @Test
    void toCityShouldMapFieldsAndCountyId() {
        County county = mock(County.class);
        when(county.getId()).thenReturn(COUNTY_ID_REF);

        City city = mock(City.class);
        when(city.getId()).thenReturn(CITY_ID);
        when(city.getName()).thenReturn(CITY_NAME);
        when(city.getCounty()).thenReturn(county);

        CityResponse result = geographyMapper.toCity(city);

        assertNotNull(result);
        assertEquals(CITY_ID, result.id());
        assertEquals(CITY_NAME, result.name());
        assertEquals(COUNTY_ID_REF, result.countyId());
    }
}
