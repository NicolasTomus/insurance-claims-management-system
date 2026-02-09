package com.insurance.backend.web.controller.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.backend.domain.building.Building;
import com.insurance.backend.domain.building.BuildingType;
import com.insurance.backend.domain.client.Client;
import com.insurance.backend.domain.client.ClientType;
import com.insurance.backend.domain.geography.City;
import com.insurance.backend.domain.geography.County;
import com.insurance.backend.domain.geography.Country;
import com.insurance.backend.infrastructure.persistence.repository.building.BuildingRepository;
import com.insurance.backend.infrastructure.persistence.repository.client.ClientRepository;
import com.insurance.backend.infrastructure.persistence.repository.geografy.CityRepository;
import com.insurance.backend.infrastructure.persistence.repository.geografy.CountryRepository;
import com.insurance.backend.infrastructure.persistence.repository.geografy.CountyRepository;
import com.insurance.backend.web.dto.building.BuildingCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BuildingControllerIT {

    private static final String POSTGRES_IMAGE = "postgres:16";
    private static final String DB_NAME = "insurance_test";
    private static final String DB_USER = "test";
    private static final String DB_PASS = "test";

    private static final String BASE_PATH = "/api/brokers";
    private static final String CLIENT_BUILDINGS_PATH = BASE_PATH + "/clients/{clientId}/buildings";
    private static final String BUILDINGS_BY_ID_PATH = BASE_PATH + "/buildings/{id}";
    private static final String BUILDINGS_BY_BUILDING_ID_PATH = BASE_PATH + "/buildings/{buildingId}";

    private static final String VALIDATION_FAILED = "Validation failed";

    private static final long MISSING_ID = 999999L;

    private static final String COUNTRY_NAME = "Romania";
    private static final String COUNTY_NAME = "Cluj";
    private static final String CITY_NAME = "Cluj-Napoca";

    private static final ClientType CLIENT_TYPE = ClientType.INDIVIDUAL;
    private static final String CLIENT_NAME = "Popescu Ion";
    private static final String CLIENT_CNP = "1234567890123";
    private static final String CLIENT_EMAIL = "ion@test.ro";
    private static final String CLIENT_PHONE = "0712345678";
    private static final String CLIENT_ADDRESS = "Str. Test 1";

    private static final String BUILDING_ADDRESS_CREATE = "Str. Marasesti 10";
    private static final BuildingType BUILDING_TYPE_RESIDENTIAL = BuildingType.RESIDENTIAL;

    private static final int BUILDING_YEAR_2005 = 2005;
    private static final int BUILDING_YEAR_2000 = 2000;

    private static final int FLOORS_2 = 2;

    private static final BigDecimal SURFACE_120_5 = new BigDecimal("120.5");
    private static final BigDecimal SURFACE_120 = new BigDecimal("120");
    private static final BigDecimal INSURED_100000 = new BigDecimal("100000");

    private static final boolean FLOOD_FALSE = false;
    private static final boolean EARTHQUAKE_FALSE = false;

    private static final String PARAM_PAGE = "page";
    private static final String PARAM_SIZE = "size";
    private static final String PAGE_0 = "0";
    private static final String SIZE_20 = "20";

    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_NOT_FOUND = 404;

    private static final String ADDRESS_BLANK = "   ";

    private static final String UPDATE_ADDRESS_OLD = "Old Address";
    private static final String UPDATE_ADDRESS_NEW = "New Address";
    private static final String UPDATE_BUILDING_TYPE_OFFICE = "OFFICE";

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
    @Autowired ObjectMapper objectMapper;

    @Autowired BuildingRepository buildingRepository;
    @Autowired ClientRepository clientRepository;
    @Autowired CityRepository cityRepository;
    @Autowired CountyRepository countyRepository;
    @Autowired CountryRepository countryRepository;

    @BeforeEach
    void cleanDb() {
        buildingRepository.deleteAll();
        cityRepository.deleteAll();
        countyRepository.deleteAll();
        countryRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void createBuildingThenGetBuildingOk() throws Exception {
        Country country = countryRepository.save(new Country(COUNTRY_NAME));
        County county = countyRepository.save(new County(COUNTY_NAME, country));
        City city = cityRepository.save(new City(CITY_NAME, county));

        Client client = clientRepository.save(new Client(
                CLIENT_TYPE,
                CLIENT_NAME,
                CLIENT_CNP,
                CLIENT_EMAIL,
                CLIENT_PHONE,
                CLIENT_ADDRESS
        ));

        BuildingCreateRequest req = new BuildingCreateRequest(
                BUILDING_ADDRESS_CREATE,
                city.getId(),
                BUILDING_YEAR_2005,
                BUILDING_TYPE_RESIDENTIAL,
                FLOORS_2,
                SURFACE_120_5,
                INSURED_100000,
                FLOOD_FALSE,
                true
        );

        MvcResult createRes = mockMvc.perform(
                        post(CLIENT_BUILDINGS_PATH, client.getId())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.clientId").value(client.getId()))
                .andExpect(jsonPath("$.address").value(BUILDING_ADDRESS_CREATE))
                .andExpect(jsonPath("$.constructionYear").value(BUILDING_YEAR_2005))
                .andExpect(jsonPath("$.buildingType").value(BUILDING_TYPE_RESIDENTIAL.name()))
                .andExpect(jsonPath("$.geography.cityId").value(city.getId()))
                .andReturn();

        long buildingId = objectMapper.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get(BUILDINGS_BY_ID_PATH, buildingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(buildingId))
                .andExpect(jsonPath("$.clientId").value(client.getId()))
                .andExpect(jsonPath("$.geography.countryName").value(COUNTRY_NAME))
                .andExpect(jsonPath("$.geography.countyName").value(COUNTY_NAME))
                .andExpect(jsonPath("$.geography.cityName").value(CITY_NAME));
    }

    @Test
    void createBuildingInvalidRequestReturns400() throws Exception {
        Client client = clientRepository.save(new Client(
                CLIENT_TYPE,
                CLIENT_NAME,
                CLIENT_CNP,
                CLIENT_EMAIL,
                CLIENT_PHONE,
                CLIENT_ADDRESS
        ));

        BuildingCreateRequest badReq = new BuildingCreateRequest(
                ADDRESS_BLANK,
                null,
                null,
                null,
                null,
                null,
                null,
                FLOOD_FALSE,
                EARTHQUAKE_FALSE
        );

        mockMvc.perform(
                        post(CLIENT_BUILDINGS_PATH, client.getId())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(badReq))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(VALIDATION_FAILED))
                .andExpect(jsonPath("$.fieldErrors.address").exists())
                .andExpect(jsonPath("$.fieldErrors.cityId").exists())
                .andExpect(jsonPath("$.fieldErrors.constructionYear").exists())
                .andExpect(jsonPath("$.fieldErrors.buildingType").exists())
                .andExpect(jsonPath("$.fieldErrors.surfaceArea").exists())
                .andExpect(jsonPath("$.fieldErrors.insuredValue").exists());
    }

    @Test
    void createBuildingClientNotFoundReturns404() throws Exception {
        Country country = countryRepository.save(new Country(COUNTRY_NAME));
        County county = countyRepository.save(new County(COUNTY_NAME, country));
        City city = cityRepository.save(new City(CITY_NAME, county));

        BuildingCreateRequest req = new BuildingCreateRequest(
                BUILDING_ADDRESS_CREATE,
                city.getId(),
                BUILDING_YEAR_2005,
                BUILDING_TYPE_RESIDENTIAL,
                FLOORS_2,
                SURFACE_120_5,
                INSURED_100000,
                FLOOD_FALSE,
                true
        );

        mockMvc.perform(
                        post(CLIENT_BUILDINGS_PATH, MISSING_ID)
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Client not found: " + MISSING_ID));
    }

    @Test
    void getBuildingNotFoundReturns404() throws Exception {
        mockMvc.perform(get(BUILDINGS_BY_ID_PATH, MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Building not found: " + MISSING_ID));
    }

    @Test
    void listForClientReturnsPage() throws Exception {
        Country country = countryRepository.save(new Country(COUNTRY_NAME));
        County county = countyRepository.save(new County(COUNTY_NAME, country));
        City city = cityRepository.save(new City(CITY_NAME, county));

        Client client = clientRepository.save(new Client(
                CLIENT_TYPE,
                CLIENT_NAME,
                CLIENT_CNP,
                CLIENT_EMAIL,
                CLIENT_PHONE,
                CLIENT_ADDRESS
        ));

        buildingRepository.save(Building.builder()
                .owner(client)
                .address("Str. 1")
                .city(city)
                .constructionYear(BUILDING_YEAR_2000)
                .buildingType(BUILDING_TYPE_RESIDENTIAL)
                .numberOfFloors(FLOORS_2)
                .insuredValue(INSURED_100000)
                .surfaceArea(SURFACE_120)
                .floodZone(FLOOD_FALSE)
                .earthquakeRiskZone(EARTHQUAKE_FALSE)
                .build()
        );

        mockMvc.perform(get(CLIENT_BUILDINGS_PATH, client.getId())
                        .param(PARAM_PAGE, PAGE_0)
                        .param(PARAM_SIZE, SIZE_20))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void updateBuildingOk() throws Exception {
        Country country = countryRepository.save(new Country(COUNTRY_NAME));
        County county = countyRepository.save(new County(COUNTY_NAME, country));
        City city = cityRepository.save(new City(CITY_NAME, county));

        Client client = clientRepository.save(new Client(
                CLIENT_TYPE,
                CLIENT_NAME,
                CLIENT_CNP,
                CLIENT_EMAIL,
                CLIENT_PHONE,
                CLIENT_ADDRESS
        ));

        Building building = buildingRepository.save(Building.builder()
                .owner(client)
                .address(UPDATE_ADDRESS_OLD)
                .city(city)
                .constructionYear(BUILDING_YEAR_2000)
                .buildingType(BUILDING_TYPE_RESIDENTIAL)
                .numberOfFloors(FLOORS_2)
                .insuredValue(INSURED_100000)
                .surfaceArea(SURFACE_120)
                .floodZone(FLOOD_FALSE)
                .earthquakeRiskZone(EARTHQUAKE_FALSE)
                .build()
        );

        String updateJson = """
                {
                  "address": "%s",
                  "cityId": %d,
                  "constructionYear": 2005,
                  "buildingType": "%s",
                  "numberOfFloors": 3,
                  "surfaceArea": 150.0,
                  "insuredValue": 120000.0,
                  "floodZone": true,
                  "earthquakeRiskZone": true
                }
                """.formatted(UPDATE_ADDRESS_NEW, city.getId(), UPDATE_BUILDING_TYPE_OFFICE);

        mockMvc.perform(put(BUILDINGS_BY_BUILDING_ID_PATH, building.getId())
                        .contentType(APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(building.getId()))
                .andExpect(jsonPath("$.address").value(UPDATE_ADDRESS_NEW))
                .andExpect(jsonPath("$.buildingType").value(UPDATE_BUILDING_TYPE_OFFICE));
    }
}
