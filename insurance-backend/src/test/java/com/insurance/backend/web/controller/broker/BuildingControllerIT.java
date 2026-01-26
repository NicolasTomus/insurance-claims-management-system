package com.insurance.backend.web.controller.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.backend.domain.building.BuildingType;
import com.insurance.backend.domain.client.Client;
import com.insurance.backend.domain.client.ClientType;
import com.insurance.backend.domain.geography.City;
import com.insurance.backend.domain.geography.County;
import com.insurance.backend.domain.geography.Country;
import com.insurance.backend.infrastructure.persistence.repository.*;
import com.insurance.backend.web.dto.building.BuildingCreateRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

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
    void createBuilding_thenGetBuilding_ok() throws Exception {
        Country country = countryRepository.save(new Country("Romania"));
        County county = countyRepository.save(new County("Cluj", country));
        City city = cityRepository.save(new City("Cluj-Napoca", county));

        Client client = clientRepository.save(new Client(
                ClientType.INDIVIDUAL,
                "Popescu Ion",
                "1234567890123",
                "ion@test.ro",
                "0712345678",
                "Str. Test 1"
        ));

        BuildingCreateRequest req = new BuildingCreateRequest(
                "Str. Marasesti 10",
                city.getId(),
                2005,
                BuildingType.RESIDENTIAL,
                2,
                new BigDecimal("120.5"),
                new BigDecimal("100000"),
                false,
                true
        );

        // /api/brokers/clients/1/buildings
        MvcResult createRes = mockMvc.perform(
                        post("/api/brokers/clients/{clientId}/buildings", client.getId())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.clientId").value(client.getId()))
                .andExpect(jsonPath("$.address").value("Str. Marasesti 10"))
                .andExpect(jsonPath("$.constructionYear").value(2005))
                .andExpect(jsonPath("$.buildingType").value("RESIDENTIAL"))
                .andExpect(jsonPath("$.geography.cityId").value(city.getId()))
                .andReturn();

        Long buildingId = objectMapper.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        // /api/brokers/buildings/1
        mockMvc.perform(get("/api/brokers/buildings/{id}", buildingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(buildingId))
                .andExpect(jsonPath("$.clientId").value(client.getId()))
                .andExpect(jsonPath("$.geography.countryName").value("Romania"))
                .andExpect(jsonPath("$.geography.countyName").value("Cluj"))
                .andExpect(jsonPath("$.geography.cityName").value("Cluj-Napoca"));
    }

    @Test
    void createBuilding_invalidRequest_returns400() throws Exception {
        Client client = clientRepository.save(new Client(
                ClientType.INDIVIDUAL,
                "Popescu Ion",
                "1234567890123",
                "ion@test.ro",
                "0712345678",
                "Str. Test 1"
        ));

        BuildingCreateRequest badReq = new BuildingCreateRequest(
                "   ",
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                false
        );

        mockMvc.perform(
                        post("/api/brokers/clients/{clientId}/buildings", client.getId())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(badReq))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.address").exists())
                .andExpect(jsonPath("$.fieldErrors.cityId").exists())
                .andExpect(jsonPath("$.fieldErrors.constructionYear").exists())
                .andExpect(jsonPath("$.fieldErrors.buildingType").exists())
                .andExpect(jsonPath("$.fieldErrors.surfaceArea").exists())
                .andExpect(jsonPath("$.fieldErrors.insuredValue").exists());
    }

    @Test
    void createBuilding_clientNotFound_returns404() throws Exception {
        Country country = countryRepository.save(new Country("Romania"));
        County county = countyRepository.save(new County("Cluj", country));
        City city = cityRepository.save(new City("Cluj-Napoca", county));

        BuildingCreateRequest req = new BuildingCreateRequest(
                "Str. Marasesti 10",
                city.getId(),
                2005,
                BuildingType.RESIDENTIAL,
                2,
                new BigDecimal("120.5"),
                new BigDecimal("100000"),
                false,
                true
        );

        long missingClientId = 999999L;

        mockMvc.perform(
                        post("/api/brokers/clients/{clientId}/buildings", missingClientId)
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Client not found: " + missingClientId));
    }

    @Test
    void getBuilding_notFound_returns404() throws Exception {
        long missingBuildingId = 999999L;

        mockMvc.perform(get("/api/brokers/buildings/{id}", missingBuildingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Building not found: " + missingBuildingId));
    }

}
