package com.insurance.backend.web.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.backend.domain.metadata.fee.FeeConfiguration;
import com.insurance.backend.domain.metadata.fee.FeeType;
import com.insurance.backend.infrastructure.persistence.repository.metadata.fee.FeeConfigurationRepository;
import com.insurance.backend.web.dto.metadata.fee.FeeConfigurationCreateRequest;
import com.insurance.backend.web.dto.metadata.fee.FeeConfigurationUpdateRequest;
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
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminFeeConfigurationControllerIT {

    private static final String POSTGRES_IMAGE = "postgres:16";
    private static final String DB_NAME = "insurance_test";
    private static final String DB_USER = "test";
    private static final String DB_PASS = "test";

    private static final String BASE_PATH = "/api/admin/fees";
    private static final String BY_ID_PATH = BASE_PATH + "/{feeId}";

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
    @Autowired private FeeConfigurationRepository feeRepository;

    @BeforeEach
    void cleanDb() {
        feeRepository.deleteAll();
    }

    @Test
    void createFeeOkReturns201() throws Exception {
        FeeConfigurationCreateRequest req = new FeeConfigurationCreateRequest(
                "  Admin Fee  ",
                FeeType.ADMIN_FEE,
                new BigDecimal("0.1000"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                true
        );

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Admin Fee"))
                .andExpect(jsonPath("$.type").value(FeeType.ADMIN_FEE.name()))
                .andExpect(jsonPath("$.percentage").value(0.1000))
                .andExpect(jsonPath("$.effectiveFrom").value("2026-01-01"))
                .andExpect(jsonPath("$.effectiveTo").value("2026-12-31"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getFeeOkReturns200() throws Exception {
        FeeConfiguration saved = feeRepository.save(new FeeConfiguration(
                "Admin Fee",
                FeeType.ADMIN_FEE,
                new BigDecimal("0.1000"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                true
        ));

        mockMvc.perform(get(BY_ID_PATH, saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Admin Fee"))
                .andExpect(jsonPath("$.type").value(FeeType.ADMIN_FEE.name()));
    }

    @Test
    void updateFeeOkReturns200() throws Exception {
        FeeConfiguration saved = feeRepository.save(new FeeConfiguration(
                "Admin Fee",
                FeeType.ADMIN_FEE,
                new BigDecimal("0.1000"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                true
        ));

        FeeConfigurationUpdateRequest update = new FeeConfigurationUpdateRequest(
                "  Updated Fee  ",
                FeeType.BROKER_COMMISSION,
                new BigDecimal("0.0500"),
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 11, 1),
                false
        );

        mockMvc.perform(put(BY_ID_PATH, saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Updated Fee"))
                .andExpect(jsonPath("$.type").value(FeeType.BROKER_COMMISSION.name()))
                .andExpect(jsonPath("$.percentage").value(0.0500))
                .andExpect(jsonPath("$.effectiveFrom").value("2026-02-01"))
                .andExpect(jsonPath("$.effectiveTo").value("2026-11-01"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void listFeesReturns200Page() throws Exception {
        feeRepository.save(new FeeConfiguration("A", FeeType.ADMIN_FEE, new BigDecimal("0.1000"), null, null, true));
        feeRepository.save(new FeeConfiguration("B", FeeType.BROKER_COMMISSION, new BigDecimal("0.0500"), null, null, true));

        mockMvc.perform(get(BASE_PATH)
                        .param(PARAM_PAGE, PAGE_0)
                        .param(PARAM_SIZE, SIZE_20))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void createFeeInvalidReturns400() throws Exception {
        FeeConfigurationCreateRequest badReq = new FeeConfigurationCreateRequest(
                " ",
                null,
                new BigDecimal("0.0"),
                null,
                null,
                true
        );

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(badReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_FAILED))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void getFeeNotFoundReturns404() throws Exception {
        mockMvc.perform(get(BY_ID_PATH, MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Fee configuration not found: " + MISSING_ID))
                .andExpect(jsonPath("$.path").value(BASE_PATH + "/" + MISSING_ID));
    }

    private String asJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
