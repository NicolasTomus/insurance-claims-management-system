package com.insurance.backend.web.controller.broker;

import com.insurance.backend.service.GeographyService;
import com.insurance.backend.web.dto.geography.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brokers")
public class BrokerGeographyController {

    private final GeographyService geographyService;

    public BrokerGeographyController(GeographyService geographyService) {
        this.geographyService = geographyService;
    }

    @GetMapping("/countries")
    public List<CountryResponse> countries() {
        return geographyService.listCountries();
    }

    @GetMapping("/countries/{countryId}/counties")
    public List<CountyResponse> counties(@PathVariable Long countryId) {
        return geographyService.listCounties(countryId);
    }

    @GetMapping("/counties/{countyId}/cities")
    public List<CityResponse> cities(@PathVariable Long countyId) {
        return geographyService.listCities(countyId);
    }
}
