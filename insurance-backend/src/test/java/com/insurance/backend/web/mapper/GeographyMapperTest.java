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

    private final GeographyMapper geographyMapper = new GeographyMapper();

    @Test
    void toCountry_shouldMapFields() {
        Country c = mock(Country.class);
        when(c.getId()).thenReturn(1L);
        when(c.getName()).thenReturn("Romania");

        CountryResponse result = geographyMapper.toCountry(c);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Romania", result.name());
    }

    @Test
    void toCounty_shouldMapFields_andCountryId() {
        Country country = mock(Country.class);
        when(country.getId()).thenReturn(10L);

        County county = mock(County.class);
        when(county.getId()).thenReturn(2L);
        when(county.getName()).thenReturn("Cluj");
        when(county.getCountry()).thenReturn(country);

        CountyResponse result = geographyMapper.toCounty(county);

        assertNotNull(result);
        assertEquals(2L, result.id());
        assertEquals("Cluj", result.name());
        assertEquals(10L, result.countryId());
    }

    @Test
    void toCity_shouldMapFields_andCountyId() {
        County county = mock(County.class);
        when(county.getId()).thenReturn(20L);

        City city = mock(City.class);
        when(city.getId()).thenReturn(3L);
        when(city.getName()).thenReturn("Cluj-Napoca");
        when(city.getCounty()).thenReturn(county);

        CityResponse result = geographyMapper.toCity(city);

        assertNotNull(result);
        assertEquals(3L, result.id());
        assertEquals("Cluj-Napoca", result.name());
        assertEquals(20L, result.countyId());
    }
}
