package com.insurance.backend.web.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.backend.domain.metadata.currency.Currency;
import com.insurance.backend.infrastructure.persistence.repository.metadata.currency.CurrencyRepository;
import com.insurance.backend.web.dto.metadata.currency.CurrencyCreateRequest;
import com.insurance.backend.web.dto.metadata.currency.CurrencyUpdateRequest;
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
class AdminCurrencyControllerIT {

    private static final String POSTGRES_IMAGE = "postgres:16";
    private static final String DB_NAME = "insurance_test";
    private static final String DB_USER = "test";
    private static final String DB_PASS = "test";

    private static final String BASE_PATH = "/api/admin/currencies";
    private static final String BY_ID_PATH = BASE_PATH + "/{currencyId}";

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
    @Autowired private CurrencyRepository currencyRepository;

    @BeforeEach
    void cleanDb() {
        currencyRepository.deleteAll();
    }

    @Test
    void createCurrencyOkReturns201() throws Exception {
        CurrencyCreateRequest req = new CurrencyCreateRequest(
                " eur ",
                "Euro",
                new BigDecimal("1.00"),
                true
        );

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").value("EUR"))
                .andExpect(jsonPath("$.name").value("Euro"))
                .andExpect(jsonPath("$.exchangeRateToBase").value(1.00))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createCurrencyDuplicateCodeReturns409() throws Exception {
        currencyRepository.save(new Currency("EUR", "Euro", new BigDecimal("1.00"), true));

        CurrencyCreateRequest req = new CurrencyCreateRequest(
                "eur",
                "Euro2",
                new BigDecimal("1.00"),
                true
        );

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Currency code already exists: EUR"))
                .andExpect(jsonPath("$.path").value(BASE_PATH));
    }

    @Test
    void getCurrencyOkReturns200() throws Exception {
        Currency saved = currencyRepository.save(new Currency("EUR", "Euro", new BigDecimal("1.00"), true));

        mockMvc.perform(get(BY_ID_PATH, saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.code").value("EUR"));
    }

    @Test
    void updateCurrencyOkReturns200() throws Exception {
        Currency saved = currencyRepository.save(new Currency("EUR", "Euro", new BigDecimal("1.00"), true));

        CurrencyUpdateRequest update = new CurrencyUpdateRequest(
                "Euro Updated",
                new BigDecimal("1.10"),
                false
        );

        mockMvc.perform(put(BY_ID_PATH, saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Euro Updated"))
                .andExpect(jsonPath("$.exchangeRateToBase").value(1.10))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void listCurrenciesReturns200Page() throws Exception {
        currencyRepository.save(new Currency("EUR", "Euro", new BigDecimal("1.00"), true));
        currencyRepository.save(new Currency("USD", "Dollar", new BigDecimal("0.90"), true));

        mockMvc.perform(get(BASE_PATH)
                        .param(PARAM_PAGE, PAGE_0)
                        .param(PARAM_SIZE, SIZE_20))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void createCurrencyInvalidReturns400() throws Exception {
        CurrencyCreateRequest badReq = new CurrencyCreateRequest(
                "  ",
                "x".repeat(101),
                new BigDecimal("0.0"),
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
    void createCurrencyInvalidNullCodeCoversCodeNullBranch() throws Exception {
        String json = """
                {
                  "code": null,
                  "name": "Euro",
                  "exchangeRateToBase": 1.00,
                  "active": true
                }
                """;

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_FAILED))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void createCurrencyInvalidNullNameCoversNameNullBranch() throws Exception {
        String json = """
                {
                  "code": "eur",
                  "name": null,
                  "exchangeRateToBase": 1.00,
                  "active": true
                }
                """;

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_FAILED))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void getCurrencyNotFoundReturns404() throws Exception {
        mockMvc.perform(get(BY_ID_PATH, MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Currency not found: " + MISSING_ID))
                .andExpect(jsonPath("$.path").value(BASE_PATH + "/" + MISSING_ID));
    }

    private String asJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
