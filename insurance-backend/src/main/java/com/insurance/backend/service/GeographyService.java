package com.insurance.backend.service;

import com.insurance.backend.infrastructure.persistence.repository.geografy.CityRepository;
import com.insurance.backend.infrastructure.persistence.repository.geografy.CountyRepository;
import com.insurance.backend.infrastructure.persistence.repository.geografy.CountryRepository;
import com.insurance.backend.web.dto.geography.CityResponse;
import com.insurance.backend.web.dto.geography.CountyResponse;
import com.insurance.backend.web.dto.geography.CountryResponse;
import com.insurance.backend.web.exception.NotFoundException;
import com.insurance.backend.web.mapper.GeographyMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GeographyService {

    private final CountryRepository countryRepository;
    private final CountyRepository countyRepository;
    private final CityRepository cityRepository;
    private final GeographyMapper geographyMapper;

    public GeographyService(CountryRepository countryRepository, CountyRepository countyRepository,
                            CityRepository cityRepository, GeographyMapper geographyMapper) {
        this.countryRepository = countryRepository;
        this.countyRepository = countyRepository;
        this.cityRepository = cityRepository;
        this.geographyMapper = geographyMapper;
    }

    @Transactional(readOnly = true)
    public List<CountryResponse> listCountries() {
        return countryRepository.findAll().stream()
                .map(geographyMapper::toCountry)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CountyResponse> listCounties(Long countryId) {
        if (!countryRepository.existsById(countryId)) {
            throw new NotFoundException("Country not found: " + countryId);
        }
        return countyRepository.findByCountryId(countryId).stream()
                .map(geographyMapper::toCounty)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CityResponse> listCities(Long countyId) {
        if (!countyRepository.existsById(countyId)) {
            throw new NotFoundException("County not found: " + countyId);
        }
        return cityRepository.findByCountyId(countyId).stream()
                .map(geographyMapper::toCity)
                .toList();
    }
}
