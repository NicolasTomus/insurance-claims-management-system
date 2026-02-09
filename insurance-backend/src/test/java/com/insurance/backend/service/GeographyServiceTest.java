package com.insurance.backend.service;

import com.insurance.backend.domain.geography.City;
import com.insurance.backend.domain.geography.County;
import com.insurance.backend.domain.geography.Country;
import com.insurance.backend.infrastructure.persistence.repository.geografy.CityRepository;
import com.insurance.backend.infrastructure.persistence.repository.geografy.CountyRepository;
import com.insurance.backend.infrastructure.persistence.repository.geografy.CountryRepository;
import com.insurance.backend.web.dto.geography.CityResponse;
import com.insurance.backend.web.dto.geography.CountyResponse;
import com.insurance.backend.web.dto.geography.CountryResponse;
import com.insurance.backend.web.exception.NotFoundException;
import com.insurance.backend.web.mapper.GeographyMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeographyServiceTest {

    private static final long COUNTRY_ID = 10L;
    private static final long COUNTY_ID = 99L;

    private static final long ROMANIA_ID = 1L;
    private static final long BULGARIA_ID = 2L;

    private static final long CLUJ_ID = 1L;
    private static final long BIHOR_ID = 2L;

    private static final long CLUJ_NAPOCA_ID = 1L;
    private static final long ORADEA_ID = 2L;

    private static final String ROMANIA = "Romania";
    private static final String BULGARIA = "Bulgaria";

    private static final String CLUJ = "Cluj";
    private static final String BIHOR = "Bihor";

    private static final String CLUJ_NAPOCA = "Cluj-Napoca";
    private static final String ORADEA = "Oradea";

    private static final String NAME_RO = "RO";
    private static final String NAME_HUNGARY = "Hungary";
    private static final String NAME_CJ = "CJ";
    private static final String NAME_CJ_N = "CJ-N";

    private static final boolean EXPECT_EMPTY = true;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private CountyRepository countyRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private GeographyMapper geographyMapper;

    @InjectMocks
    private GeographyService geographyService;

    @Test
    void listCountriesShouldReturnMappedList() {
        Country c1 = mock(Country.class);
        Country c2 = mock(Country.class);

        when(countryRepository.findAll()).thenReturn(List.of(c1, c2));

        CountryResponse r1 = new CountryResponse(ROMANIA_ID, ROMANIA);
        CountryResponse r2 = new CountryResponse(BULGARIA_ID, BULGARIA);

        when(geographyMapper.toCountry(c1)).thenReturn(r1);
        when(geographyMapper.toCountry(c2)).thenReturn(r2);

        List<CountryResponse> result = geographyService.listCountries();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertSame(r1, result.get(0));
        assertSame(r2, result.get(1));

        verify(countryRepository).findAll();
        verify(geographyMapper).toCountry(c1);
        verify(geographyMapper).toCountry(c2);
    }

    @Test
    void listCountiesShouldThrowWhenCountryNotFound() {
        when(countryRepository.existsById(COUNTRY_ID)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> geographyService.listCounties(COUNTRY_ID));

        verify(countryRepository).existsById(COUNTRY_ID);
        verify(countyRepository, never()).findByCountryId(anyLong());
        verifyNoInteractions(geographyMapper);
    }

    @Test
    void listCountiesShouldReturnMappedListWhenOk() {
        when(countryRepository.existsById(COUNTRY_ID)).thenReturn(true);

        County co1 = mock(County.class);
        County co2 = mock(County.class);

        when(countyRepository.findByCountryId(COUNTRY_ID)).thenReturn(List.of(co1, co2));

        CountyResponse r1 = new CountyResponse(CLUJ_ID, CLUJ, COUNTRY_ID);
        CountyResponse r2 = new CountyResponse(BIHOR_ID, BIHOR, COUNTRY_ID);

        when(geographyMapper.toCounty(co1)).thenReturn(r1);
        when(geographyMapper.toCounty(co2)).thenReturn(r2);

        List<CountyResponse> result = geographyService.listCounties(COUNTRY_ID);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertSame(r1, result.get(0));
        assertSame(r2, result.get(1));

        verify(countryRepository).existsById(COUNTRY_ID);
        verify(countyRepository).findByCountryId(COUNTRY_ID);
        verify(geographyMapper).toCounty(co1);
        verify(geographyMapper).toCounty(co2);
    }

    @Test
    void listCitiesShouldThrowWhenCountyNotFound() {
        when(countyRepository.existsById(COUNTY_ID)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> geographyService.listCities(COUNTY_ID));

        verify(countyRepository).existsById(COUNTY_ID);
        verify(cityRepository, never()).findByCountyId(anyLong());
        verifyNoInteractions(geographyMapper);
    }

    @Test
    void listCitiesShouldReturnMappedListWhenOk() {
        when(countyRepository.existsById(COUNTY_ID)).thenReturn(true);

        City city1 = mock(City.class);
        City city2 = mock(City.class);

        when(cityRepository.findByCountyId(COUNTY_ID)).thenReturn(List.of(city1, city2));

        CityResponse r1 = new CityResponse(CLUJ_NAPOCA_ID, CLUJ_NAPOCA, COUNTY_ID);
        CityResponse r2 = new CityResponse(ORADEA_ID, ORADEA, COUNTY_ID);

        when(geographyMapper.toCity(city1)).thenReturn(r1);
        when(geographyMapper.toCity(city2)).thenReturn(r2);

        List<CityResponse> result = geographyService.listCities(COUNTY_ID);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertSame(r1, result.get(0));
        assertSame(r2, result.get(1));

        verify(countyRepository).existsById(COUNTY_ID);
        verify(cityRepository).findByCountyId(COUNTY_ID);
        verify(geographyMapper).toCity(city1);
        verify(geographyMapper).toCity(city2);
    }

    @Test
    void countryGettersSettersAndCountiesListAreCovered() {
        Country country = new Country(ROMANIA);
        assertEquals(ROMANIA, country.getName());

        country.setName(NAME_RO);
        assertEquals(NAME_RO, country.getName());

        assertNotNull(country.getCounties());
        assertEquals(EXPECT_EMPTY, country.getCounties().isEmpty());
    }

    @Test
    void countyGettersSettersAndCitiesListAreCovered() {
        Country country = new Country(ROMANIA);
        County county = new County(CLUJ, country);

        assertEquals(CLUJ, county.getName());
        assertSame(country, county.getCountry());

        county.setName(NAME_CJ);
        assertEquals(NAME_CJ, county.getName());

        Country newCountry = new Country(NAME_HUNGARY);
        county.setCountry(newCountry);
        assertSame(newCountry, county.getCountry());

        assertNotNull(county.getCities());
        assertEquals(EXPECT_EMPTY, county.getCities().isEmpty());
    }

    @Test
    void cityGettersSettersAndBuildingsListAreCovered() {
        Country country = new Country(ROMANIA);
        County county = new County(CLUJ, country);
        City city = new City(CLUJ_NAPOCA, county);

        assertEquals(CLUJ_NAPOCA, city.getName());
        assertSame(county, city.getCounty());

        city.setName(NAME_CJ_N);
        assertEquals(NAME_CJ_N, city.getName());

        County newCounty = new County(BIHOR, country);
        city.setCounty(newCounty);
        assertSame(newCounty, city.getCounty());

        assertNotNull(city.getBuildings());
        assertEquals(EXPECT_EMPTY, city.getBuildings().isEmpty());
    }
}
