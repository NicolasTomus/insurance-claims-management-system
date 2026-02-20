package com.insurance.backend.web.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.backend.infrastructure.persistence.repository.metadata.fee.FeeConfigurationRepository;
import com.insurance.backend.infrastructure.persistence.repository.metadata.risk.RiskFactorConfigurationRepository;
import com.insurance.backend.infrastructure.persistence.repository.policy.PolicyRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminReportControllerIT {

    private static final String POSTGRES_IMAGE = "postgres:16";
    private static final String DB_NAME = "insurance_test";
    private static final String DB_USER = "test";
    private static final String DB_PASS = "test";

    private static final String BASE_PATH = "/api/admin/reports";
    private static final String BY_COUNTRY = BASE_PATH + "/policies-by-country";
    private static final String BY_COUNTY  = BASE_PATH + "/policies-by-county";
    private static final String BY_CITY    = BASE_PATH + "/policies-by-city";
    private static final String BY_BROKER  = BASE_PATH + "/policies-by-broker";

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


    @Autowired private PolicyRepository policyRepository;

    @Autowired(required = false) private FeeConfigurationRepository feeConfigurationRepository;
    @Autowired(required = false) private RiskFactorConfigurationRepository riskFactorConfigurationRepository;

    @BeforeEach
    void cleanDb() {
        if (policyRepository != null) policyRepository.deleteAll();

        if (feeConfigurationRepository != null) feeConfigurationRepository.deleteAll();
        if (riskFactorConfigurationRepository != null) riskFactorConfigurationRepository.deleteAll();
    }

    @Test
    void byCountryEmptyDbReturns200AndEmptyArray() throws Exception {
        mockMvc.perform(get(BY_COUNTRY))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void byCityInvalidDateRangeReturns400() throws Exception {
        mockMvc.perform(get(BY_CITY)
                        .param("from", "2026-02-10")
                        .param("to", "2026-02-01"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("from must be <= to"))
                .andExpect(jsonPath("$.path").value(BY_CITY))
                .andExpect(jsonPath("$.fieldErrors").isMap());
    }

    @Test
    void byBrokerCurrencyIsNormalizedWhitespaceAndCaseReturns200() throws Exception {
        mockMvc.perform(get(BY_BROKER)
                        .param("currency", "  eur  "))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void byCountySupportsOptionalFiltersReturns200() throws Exception {
        mockMvc.perform(get(BY_COUNTY)
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
