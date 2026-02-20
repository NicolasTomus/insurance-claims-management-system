package com.insurance.backend.web.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;
import com.insurance.backend.domain.metadata.risk.RiskLevel;
import com.insurance.backend.infrastructure.persistence.repository.metadata.risk.RiskFactorConfigurationRepository;
import com.insurance.backend.web.dto.metadata.risk.RiskFactorCreateRequest;
import com.insurance.backend.web.dto.metadata.risk.RiskFactorUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminRiskFactorControllerIT {

    private static final String POSTGRES_IMAGE = "postgres:16";
    private static final String DB_NAME = "insurance_test";
    private static final String DB_USER = "test";
    private static final String DB_PASS = "test";

    private static final String BASE_PATH = "/api/admin/risk-factors";
    private static final String BY_ID_PATH = BASE_PATH + "/{riskFactorId}";

    private static final String PARAM_PAGE = "page";
    private static final String PARAM_SIZE = "size";

    private static final String PAGE_0 = "0";
    private static final String SIZE_20 = "20";

    private static final String VALIDATION_FAILED = "Validation failed";
    private static final long MISSING_ID = 999999L;

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USER)
            .withPassword(DB_PASS);

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RiskFactorConfigurationRepository riskRepository;

    @BeforeEach
    void cleanDb() {
        riskRepository.deleteAll();
    }

    @Test
    void createRiskFactorOkReturns201() throws Exception {
        RiskFactorCreateRequest req = new RiskFactorCreateRequest(
                RiskLevel.BUILDING_TYPE,
                1L,
                new BigDecimal("0.2500"),
                true
        );

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.level").value(RiskLevel.BUILDING_TYPE.name()))
                .andExpect(jsonPath("$.referenceId").value(1))
                .andExpect(jsonPath("$.adjustmentPercentage").value(0.2500))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createRiskFactorDuplicateActiveReturns409() throws Exception {
        riskRepository.save(new RiskFactorConfiguration(
                RiskLevel.BUILDING_TYPE,
                1L,
                new BigDecimal("0.1000"),
                true
        ));

        RiskFactorCreateRequest req = new RiskFactorCreateRequest(
                RiskLevel.BUILDING_TYPE,
                1L,
                new BigDecimal("0.2000"),
                true
        );

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Active risk factor already exists for this " + RiskLevel.BUILDING_TYPE))
                .andExpect(jsonPath("$.path").value(BASE_PATH));
    }

    @Test
    void createRiskFactorMissingReferenceIdReturns400() throws Exception {
        RiskFactorCreateRequest req = new RiskFactorCreateRequest(
                RiskLevel.BUILDING_TYPE,
                null,
                new BigDecimal("0.1000"),
                true
        );

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("referenceId is required"))
                .andExpect(jsonPath("$.path").value(BASE_PATH));
    }

    @Test
    void createRiskFactorInvalidReturns400() throws Exception {
        String badJson = "{\"level\":null,\"referenceId\":1,\"adjustmentPercentage\":11.0,\"active\":true}";

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_FAILED))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void getRiskFactorOkReturns200() throws Exception {
        RiskFactorConfiguration saved = riskRepository.save(new RiskFactorConfiguration(
                RiskLevel.BUILDING_TYPE,
                1L,
                new BigDecimal("0.1000"),
                true
        ));

        mockMvc.perform(get(BY_ID_PATH, saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.level").value(RiskLevel.BUILDING_TYPE.name()))
                .andExpect(jsonPath("$.referenceId").value(1))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getRiskFactorNotFoundReturns404() throws Exception {
        mockMvc.perform(get(BY_ID_PATH, MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Risk factor not found: " + MISSING_ID))
                .andExpect(jsonPath("$.path").value(BASE_PATH + "/" + MISSING_ID));
    }

    @Test
    void updateRiskFactorOkReturns200() throws Exception {
        RiskFactorConfiguration saved = riskRepository.save(new RiskFactorConfiguration(
                RiskLevel.BUILDING_TYPE,
                1L,
                new BigDecimal("0.1000"),
                true
        ));

        RiskFactorUpdateRequest update = new RiskFactorUpdateRequest(
                RiskLevel.BUILDING_TYPE,
                2L,
                new BigDecimal("0.5000"),
                false
        );

        mockMvc.perform(put(BY_ID_PATH, saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.level").value(RiskLevel.BUILDING_TYPE.name()))
                .andExpect(jsonPath("$.referenceId").value(2))
                .andExpect(jsonPath("$.adjustmentPercentage").value(0.5000))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateRiskFactorInvalidReturns400() throws Exception {
        RiskFactorConfiguration saved = riskRepository.save(new RiskFactorConfiguration(
                RiskLevel.BUILDING_TYPE,
                1L,
                new BigDecimal("0.1000"),
                true
        ));

        RiskFactorUpdateRequest badUpdate = new RiskFactorUpdateRequest(
                null,
                null,
                new BigDecimal("11.0000"),
                null
        );

        mockMvc.perform(put(BY_ID_PATH, saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(badUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_FAILED))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void listRiskFactorsReturns200Page() throws Exception {
        RiskFactorConfiguration r1 = riskRepository.save(new RiskFactorConfiguration(
                RiskLevel.BUILDING_TYPE,
                1L,
                new BigDecimal("0.1000"),
                true
        ));
        RiskFactorConfiguration r2 = riskRepository.save(new RiskFactorConfiguration(
                RiskLevel.BUILDING_TYPE,
                2L,
                new BigDecimal("0.2000"),
                false
        ));

        mockMvc.perform(get(BASE_PATH)
                        .param(PARAM_PAGE, PAGE_0)
                        .param(PARAM_SIZE, SIZE_20))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(r1.getId()))
                .andExpect(jsonPath("$.content[1].id").value(r2.getId()));
    }

    private String asJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
