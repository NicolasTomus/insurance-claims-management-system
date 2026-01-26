package com.insurance.backend.service;

import com.insurance.backend.domain.geography.City;
import com.insurance.backend.domain.geography.County;
import com.insurance.backend.domain.geography.Country;
import com.insurance.backend.infrastructure.persistence.repository.CityRepository;
import com.insurance.backend.infrastructure.persistence.repository.CountyRepository;
import com.insurance.backend.infrastructure.persistence.repository.CountryRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeographyServiceTest {

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
    void listCountries_shouldReturnMappedList() {
        Country c1 = mock(Country.class);
        Country c2 = mock(Country.class);

        when(countryRepository.findAll()).thenReturn(List.of(c1, c2));

        CountryResponse r1 = new CountryResponse(1L, "Romania");
        CountryResponse r2 = new CountryResponse(2L, "Bulgaria");

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
    void listCounties_shouldThrow_whenCountryNotFound() {
        Long countryId = 10L;

        when(countryRepository.existsById(countryId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> geographyService.listCounties(countryId));

        verify(countryRepository).existsById(countryId);
        verify(countyRepository, never()).findByCountryId(anyLong());
        verifyNoInteractions(geographyMapper);
    }

    @Test
    void listCounties_shouldReturnMappedList_whenOk() {
        Long countryId = 10L;

        when(countryRepository.existsById(countryId)).thenReturn(true);

        County co1 = mock(County.class);
        County co2 = mock(County.class);

        when(countyRepository.findByCountryId(countryId)).thenReturn(List.of(co1, co2));

        CountyResponse r1 = new CountyResponse(1L, "Cluj", countryId);
        CountyResponse r2 = new CountyResponse(2L, "Bihor", countryId);

        when(geographyMapper.toCounty(co1)).thenReturn(r1);
        when(geographyMapper.toCounty(co2)).thenReturn(r2);

        List<CountyResponse> result = geographyService.listCounties(countryId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertSame(r1, result.get(0));
        assertSame(r2, result.get(1));

        verify(countryRepository).existsById(countryId);
        verify(countyRepository).findByCountryId(countryId);
        verify(geographyMapper).toCounty(co1);
        verify(geographyMapper).toCounty(co2);
    }

    @Test
    void listCities_shouldThrow_whenCountyNotFound() {
        Long countyId = 99L;

        when(countyRepository.existsById(countyId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> geographyService.listCities(countyId));

        verify(countyRepository).existsById(countyId);
        verify(cityRepository, never()).findByCountyId(anyLong());
        verifyNoInteractions(geographyMapper);
    }

    @Test
    void listCities_shouldReturnMappedList_whenOk() {
        Long countyId = 99L;

        when(countyRepository.existsById(countyId)).thenReturn(true);

        City city1 = mock(City.class);
        City city2 = mock(City.class);

        when(cityRepository.findByCountyId(countyId)).thenReturn(List.of(city1, city2));

        CityResponse r1 = new CityResponse(1L, "Cluj-Napoca", countyId);
        CityResponse r2 = new CityResponse(2L, "Oradea", countyId);

        when(geographyMapper.toCity(city1)).thenReturn(r1);
        when(geographyMapper.toCity(city2)).thenReturn(r2);

        List<CityResponse> result = geographyService.listCities(countyId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertSame(r1, result.get(0));
        assertSame(r2, result.get(1));

        verify(countyRepository).existsById(countyId);
        verify(cityRepository).findByCountyId(countyId);
        verify(geographyMapper).toCity(city1);
        verify(geographyMapper).toCity(city2);
    }
}
