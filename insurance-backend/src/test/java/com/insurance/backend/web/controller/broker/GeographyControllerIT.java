package com.insurance.backend.web.controller.broker;

import com.insurance.backend.domain.geography.City;
import com.insurance.backend.domain.geography.County;
import com.insurance.backend.domain.geography.Country;
import com.insurance.backend.infrastructure.persistence.repository.CityRepository;
import com.insurance.backend.infrastructure.persistence.repository.CountyRepository;
import com.insurance.backend.infrastructure.persistence.repository.CountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GeographyControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("insurance_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired MockMvc mockMvc;

    @Autowired CityRepository cityRepository;
    @Autowired CountyRepository countyRepository;
    @Autowired CountryRepository countryRepository;

    @BeforeEach
    void cleanDb() {
        cityRepository.deleteAll();
        countyRepository.deleteAll();
        countryRepository.deleteAll();
    }

    // /api/brokers/countries
    @Test
    void countries_ok_returns200List() throws Exception {
        Country country = countryRepository.save(new Country("Romania"));

        mockMvc.perform(get("/api/brokers/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(country.getId()))
                .andExpect(jsonPath("$[0].name").value("Romania"));
    }

    // /api/brokers/counties/1/cities
    @Test
    void counties_ok_returns200List() throws Exception {
        Country country = countryRepository.save(new Country("Romania"));
        County county = countyRepository.save(new County("Cluj", country));

        mockMvc.perform(get("/api/brokers/countries/{countryId}/counties", country.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(county.getId()))
                .andExpect(jsonPath("$[0].name").value("Cluj"))
                .andExpect(jsonPath("$[0].countryId").value(country.getId()));
    }

    // /api/brokers/countries/1/counties
    @Test
    void cities_ok_returns200List() throws Exception {
        Country country = countryRepository.save(new Country("Romania"));
        County county = countyRepository.save(new County("Cluj", country));
        City city = cityRepository.save(new City("Cluj-Napoca", county));

        mockMvc.perform(get("/api/brokers/counties/{countyId}/cities", county.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(city.getId()))
                .andExpect(jsonPath("$[0].name").value("Cluj-Napoca"))
                .andExpect(jsonPath("$[0].countyId").value(county.getId()));
    }

    @Test
    void counties_countryNotFound_returns404() throws Exception {
        long missingCountryId = 999999L;

        mockMvc.perform(get("/api/brokers/countries/{countryId}/counties", missingCountryId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Country not found: " + missingCountryId))
                .andExpect(jsonPath("$.path").value("/api/brokers/countries/" + missingCountryId + "/counties"));
    }

    @Test
    void cities_countyNotFound_returns404() throws Exception {
        long missingCountyId = 999999L;

        mockMvc.perform(get("/api/brokers/counties/{countyId}/cities", missingCountyId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("County not found: " + missingCountyId))
                .andExpect(jsonPath("$.path").value("/api/brokers/counties/" + missingCountyId + "/cities"));
    }
}
