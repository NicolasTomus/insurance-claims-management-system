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

    private static final long REF_ID_1 = 1L;
    private static final long REF_ID_2 = 2L;
    private static final long REF_ID_3 = 3L;

    private static final int REF_ID_1_INT = 1;
    private static final int REF_ID_2_INT = 2;
    private static final int REF_ID_3_INT = 3;

    private static final BigDecimal PCT_0500 = new BigDecimal("0.0500");
    private static final BigDecimal PCT_0750 = new BigDecimal("0.0750");
    private static final BigDecimal PCT_1000 = new BigDecimal("0.1000");
    private static final BigDecimal PCT_INVALID_MIN = new BigDecimal("0.0");

    private static final boolean ACTIVE_TRUE = true;
    private static final boolean ACTIVE_FALSE = false;

    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_NOT_FOUND = 404;

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
                RiskLevel.COUNTRY,
                REF_ID_1,
                PCT_0500,
                ACTIVE_TRUE
        );

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.level").value(RiskLevel.COUNTRY.name()))
                .andExpect(jsonPath("$.referenceId").value(REF_ID_1_INT))
                .andExpect(jsonPath("$.adjustmentPercentage").value(0.0500))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getRiskFactorOkReturns200() throws Exception {
        RiskFactorConfiguration saved = riskRepository.save(new RiskFactorConfiguration(
                RiskLevel.CITY,
                REF_ID_3,
                PCT_1000,
                ACTIVE_TRUE
        ));

        mockMvc.perform(get(BY_ID_PATH, saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.level").value(RiskLevel.CITY.name()))
                .andExpect(jsonPath("$.referenceId").value(REF_ID_3_INT));
    }

    @Test
    void updateRiskFactorOkReturns200() throws Exception {
        RiskFactorConfiguration saved = riskRepository.save(new RiskFactorConfiguration(
                RiskLevel.COUNTRY,
                REF_ID_1,
                PCT_0500,
                ACTIVE_TRUE
        ));

        RiskFactorUpdateRequest update = new RiskFactorUpdateRequest(
                RiskLevel.COUNTY,
                REF_ID_2,
                PCT_0750,
                ACTIVE_FALSE
        );

        mockMvc.perform(put(BY_ID_PATH, saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.level").value(RiskLevel.COUNTY.name()))
                .andExpect(jsonPath("$.referenceId").value(REF_ID_2_INT))
                .andExpect(jsonPath("$.adjustmentPercentage").value(0.0750))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void listRiskFactorsReturns200Page() throws Exception {
        riskRepository.save(new RiskFactorConfiguration(RiskLevel.COUNTRY, REF_ID_1, PCT_0500, ACTIVE_TRUE));
        riskRepository.save(new RiskFactorConfiguration(RiskLevel.CITY, REF_ID_3, PCT_1000, ACTIVE_TRUE));

        mockMvc.perform(get(BASE_PATH)
                        .param(PARAM_PAGE, PAGE_0)
                        .param(PARAM_SIZE, SIZE_20))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void createRiskFactorInvalidReturns400() throws Exception {
        RiskFactorCreateRequest badReq = new RiskFactorCreateRequest(
                null,
                null,
                PCT_INVALID_MIN,
                ACTIVE_TRUE
        );

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(badReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(VALIDATION_FAILED))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void getRiskFactorNotFoundReturns404() throws Exception {
        String expectedMessage = "Risk factor not found: " + MISSING_ID;
        String expectedPath = BASE_PATH + "/" + MISSING_ID;

        mockMvc.perform(get(BY_ID_PATH, MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.path").value(expectedPath));
    }

    private String asJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
