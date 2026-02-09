package com.insurance.backend.web.controller.broker;

import com.insurance.backend.domain.geography.City;
import com.insurance.backend.domain.geography.County;
import com.insurance.backend.domain.geography.Country;
import com.insurance.backend.infrastructure.persistence.repository.geografy.CityRepository;
import com.insurance.backend.infrastructure.persistence.repository.geografy.CountyRepository;
import com.insurance.backend.infrastructure.persistence.repository.geografy.CountryRepository;
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

    private static final String POSTGRES_IMAGE = "postgres:16";
    private static final String DB_NAME = "insurance_test";
    private static final String DB_USER = "test";
    private static final String DB_PASS = "test";

    private static final String BASE_PATH = "/api/brokers";
    private static final String COUNTRIES_PATH = BASE_PATH + "/countries";
    private static final String COUNTIES_BY_COUNTRY_PATH = BASE_PATH + "/countries/{countryId}/counties";
    private static final String CITIES_BY_COUNTY_PATH = BASE_PATH + "/counties/{countyId}/cities";

    private static final String COUNTRY_NAME = "Romania";
    private static final String COUNTY_NAME = "Cluj";
    private static final String CITY_NAME = "Cluj-Napoca";

    private static final int EXPECTED_SIZE_1 = 1;

    private static final int HTTP_NOT_FOUND = 404;
    private static final String ERROR_NOT_FOUND = "Not Found";

    private static final long MISSING_ID = 999999L;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USER)
            .withPassword(DB_PASS);

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

    @Test
    void countriesOkReturns200List() throws Exception {
        Country country = countryRepository.save(new Country(COUNTRY_NAME));

        mockMvc.perform(get(COUNTRIES_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(EXPECTED_SIZE_1))
                .andExpect(jsonPath("$[0].id").value(country.getId()))
                .andExpect(jsonPath("$[0].name").value(COUNTRY_NAME));
    }

    @Test
    void countiesOkReturns200List() throws Exception {
        Country country = countryRepository.save(new Country(COUNTRY_NAME));
        County county = countyRepository.save(new County(COUNTY_NAME, country));

        mockMvc.perform(get(COUNTIES_BY_COUNTRY_PATH, country.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(EXPECTED_SIZE_1))
                .andExpect(jsonPath("$[0].id").value(county.getId()))
                .andExpect(jsonPath("$[0].name").value(COUNTY_NAME))
                .andExpect(jsonPath("$[0].countryId").value(country.getId()));
    }

    @Test
    void citiesOkReturns200List() throws Exception {
        Country country = countryRepository.save(new Country(COUNTRY_NAME));
        County county = countyRepository.save(new County(COUNTY_NAME, country));
        City city = cityRepository.save(new City(CITY_NAME, county));

        mockMvc.perform(get(CITIES_BY_COUNTY_PATH, county.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(EXPECTED_SIZE_1))
                .andExpect(jsonPath("$[0].id").value(city.getId()))
                .andExpect(jsonPath("$[0].name").value(CITY_NAME))
                .andExpect(jsonPath("$[0].countyId").value(county.getId()));
    }

    @Test
    void countiesCountryNotFoundReturns404() throws Exception {
        String expectedMessage = "Country not found: " + MISSING_ID;
        String expectedPath = BASE_PATH + "/countries/" + MISSING_ID + "/counties";

        mockMvc.perform(get(COUNTIES_BY_COUNTRY_PATH, MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.error").value(ERROR_NOT_FOUND))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.path").value(expectedPath));
    }

    @Test
    void citiesCountyNotFoundReturns404() throws Exception {
        String expectedMessage = "County not found: " + MISSING_ID;
        String expectedPath = BASE_PATH + "/counties/" + MISSING_ID + "/cities";

        mockMvc.perform(get(CITIES_BY_COUNTY_PATH, MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.error").value(ERROR_NOT_FOUND))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.path").value(expectedPath));
    }
}
