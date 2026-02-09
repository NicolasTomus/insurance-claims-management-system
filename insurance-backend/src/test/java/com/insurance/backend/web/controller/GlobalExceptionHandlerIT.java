package com.insurance.backend.web.controller;

import com.insurance.backend.web.dto.metadata.currency.CurrencyCreateRequest;
import com.insurance.backend.web.exception.ConflictException;
import com.insurance.backend.web.exception.NotFoundException;
import jakarta.validation.Valid;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(GlobalExceptionHandlerIT.TestExceptionController.class)
class GlobalExceptionHandlerIT {

    private static final String POSTGRES_IMAGE = "postgres:16";
    private static final String DB_NAME = "insurance_test";
    private static final String DB_USER = "test";
    private static final String DB_PASS = "test";

    private static final String BASE_PATH = "/api/test/exceptions";

    private static final String VALIDATION_PATH = BASE_PATH + "/validation";
    private static final String NOT_FOUND_PATH = BASE_PATH + "/notfound";
    private static final String CONFLICT_PATH = BASE_PATH + "/conflict";
    private static final String UNEXPECTED_PATH = BASE_PATH + "/unexpected";

    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_CONFLICT = 409;
    private static final int HTTP_INTERNAL_SERVER_ERROR = 500;

    private static final String ERROR_INTERNAL_SERVER_ERROR = "Internal Server Error";

    private static final String MESSAGE_UNEXPECTED_SERVER_ERROR = "Unexpected server error";
    private static final String MESSAGE_VALIDATION_FAILED = "Validation failed";

    private static final String MESSAGE_NOT_FOUND = "NF";
    private static final String MESSAGE_CONFLICT = "CF";

    private static final String JSON_INVALID_CURRENCY_CREATE = """
            {
              "code": "  ",
              "name": " ",
              "exchangeRateToBase": null,
              "active": true
            }
            """;

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

    @Autowired MockMvc mockMvc;

    @RestController
    @RequestMapping(BASE_PATH)
    static class TestExceptionController {

        @PostMapping("/validation")
        public String validation(@Valid @RequestBody CurrencyCreateRequest req) {
            return "ok";
        }

        @GetMapping("/notfound")
        public String notFound() {
            throw new NotFoundException(MESSAGE_NOT_FOUND);
        }

        @GetMapping("/conflict")
        public String conflict() {
            throw new ConflictException(MESSAGE_CONFLICT);
        }

        @GetMapping("/unexpected")
        public String unexpected() {
            throw new RuntimeException("boom");
        }
    }

    @Test
    void handleUnexpectedReturns500AndExpectedBodyFields() throws Exception {
        mockMvc.perform(get(UNEXPECTED_PATH))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(HTTP_INTERNAL_SERVER_ERROR))
                .andExpect(jsonPath("$.error").value(ERROR_INTERNAL_SERVER_ERROR))
                .andExpect(jsonPath("$.message").value(MESSAGE_UNEXPECTED_SERVER_ERROR))
                .andExpect(jsonPath("$.path").value(UNEXPECTED_PATH))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void handleValidationReturns400() throws Exception {
        mockMvc.perform(post(VALIDATION_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_INVALID_CURRENCY_CREATE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HTTP_BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(MESSAGE_VALIDATION_FAILED))
                .andExpect(jsonPath("$.path").value(VALIDATION_PATH))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void handleNotFoundReturns404() throws Exception {
        mockMvc.perform(get(NOT_FOUND_PATH))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.message").value(MESSAGE_NOT_FOUND))
                .andExpect(jsonPath("$.path").value(NOT_FOUND_PATH));
    }

    @Test
    void handleConflictReturns409() throws Exception {
        mockMvc.perform(get(CONFLICT_PATH))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(HTTP_CONFLICT))
                .andExpect(jsonPath("$.message").value(MESSAGE_CONFLICT))
                .andExpect(jsonPath("$.path").value(CONFLICT_PATH));
    }
}
