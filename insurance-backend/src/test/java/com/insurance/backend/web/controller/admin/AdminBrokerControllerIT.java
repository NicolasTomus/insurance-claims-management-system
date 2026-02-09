package com.insurance.backend.web.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.backend.domain.broker.Broker;
import com.insurance.backend.domain.broker.BrokerStatus;
import com.insurance.backend.infrastructure.persistence.repository.broker.BrokerRepository;
import com.insurance.backend.web.dto.broker.BrokerCreateRequest;
import com.insurance.backend.web.dto.broker.BrokerUpdateRequest;
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
class AdminBrokerControllerIT {

    private static final String POSTGRES_IMAGE = "postgres:16";
    private static final String DB_NAME = "insurance_test";
    private static final String DB_USER = "test";
    private static final String DB_PASS = "test";

    private static final String BASE_PATH = "/api/admin/brokers";
    private static final String BY_ID_PATH = BASE_PATH + "/{brokerId}";
    private static final String ACTIVATE_PATH = BASE_PATH + "/{brokerId}/activate";
    private static final String DEACTIVATE_PATH = BASE_PATH + "/{brokerId}/deactivate";

    private static final String PARAM_PAGE = "page";
    private static final String PARAM_SIZE = "size";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_CODE = "brokerCode";

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
    @Autowired private BrokerRepository brokerRepository;

    @BeforeEach
    void cleanDb() {
        brokerRepository.deleteAll();
    }

    @Test
    void createBrokerOkReturns201() throws Exception {
        BrokerCreateRequest req = new BrokerCreateRequest(
                "BR-001",
                "Best Broker",
                "broker@test.ro",
                "0722222222",
                new BigDecimal("0.10")
        );

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.brokerCode").value("BR-001"))
                .andExpect(jsonPath("$.name").value("Best Broker"))
                .andExpect(jsonPath("$.email").value("broker@test.ro"))
                .andExpect(jsonPath("$.phone").value("0722222222"))
                .andExpect(jsonPath("$.status").value(BrokerStatus.ACTIVE.name()));
    }

    @Test
    void createBrokerDuplicateCodeReturns409() throws Exception {
        brokerRepository.save(new Broker(
                "BR-001",
                "Existing",
                "e@test.ro",
                "0700000000",
                BrokerStatus.ACTIVE,
                new BigDecimal("0.10")
        ));

        BrokerCreateRequest req = new BrokerCreateRequest(
                "  BR-001  ",
                "Other",
                "other@test.ro",
                "0711111111",
                new BigDecimal("0.05")
        );

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Broker code already exists: BR-001"))
                .andExpect(jsonPath("$.path").value(BASE_PATH));
    }

    @Test
    void getBrokerOkReturns200() throws Exception {
        Broker saved = brokerRepository.save(new Broker(
                "BR-001",
                "Best Broker",
                "broker@test.ro",
                "0722222222",
                BrokerStatus.ACTIVE,
                new BigDecimal("0.10")
        ));

        mockMvc.perform(get(BY_ID_PATH, saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.brokerCode").value("BR-001"));
    }

    @Test
    void updateBrokerOkReturns200() throws Exception {
        Broker saved = brokerRepository.save(new Broker(
                "BR-001",
                "Best Broker",
                "broker@test.ro",
                "0722222222",
                BrokerStatus.ACTIVE,
                new BigDecimal("0.10")
        ));

        BrokerUpdateRequest update = new BrokerUpdateRequest(
                "Best Broker Updated",
                "broker.updated@test.ro",
                "0799999999",
                BrokerStatus.INACTIVE,
                new BigDecimal("0.15")
        );

        mockMvc.perform(put(BY_ID_PATH, saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Best Broker Updated"))
                .andExpect(jsonPath("$.email").value("broker.updated@test.ro"))
                .andExpect(jsonPath("$.phone").value("0799999999"))
                .andExpect(jsonPath("$.status").value(BrokerStatus.INACTIVE.name()))
                .andExpect(jsonPath("$.commissionPercentage").value(0.15));
    }

    @Test
    void activateBrokerReturns200() throws Exception {
        Broker saved = brokerRepository.save(new Broker(
                "BR-001",
                "Best Broker",
                "broker@test.ro",
                "0722222222",
                BrokerStatus.INACTIVE,
                new BigDecimal("0.10")
        ));

        mockMvc.perform(post(ACTIVATE_PATH, saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.status").value(BrokerStatus.ACTIVE.name()));
    }

    @Test
    void deactivateBrokerReturns200() throws Exception {
        Broker saved = brokerRepository.save(new Broker(
                "BR-001",
                "Best Broker",
                "broker@test.ro",
                "0722222222",
                BrokerStatus.ACTIVE,
                new BigDecimal("0.10")
        ));

        mockMvc.perform(post(DEACTIVATE_PATH, saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.status").value(BrokerStatus.INACTIVE.name()));
    }

    @Test
    void searchNoFiltersReturns200Page() throws Exception {
        Broker b1 = brokerRepository.save(new Broker(
                "BR-001", "Alpha Broker", "a@test.ro", "0700000001", BrokerStatus.ACTIVE, new BigDecimal("0.10")
        ));
        Broker b2 = brokerRepository.save(new Broker(
                "BR-002", "Beta Broker", "b@test.ro", "0700000002", BrokerStatus.INACTIVE, new BigDecimal("0.05")
        ));

        mockMvc.perform(get(BASE_PATH)
                        .param(PARAM_PAGE, PAGE_0)
                        .param(PARAM_SIZE, SIZE_20))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(b1.getId()))
                .andExpect(jsonPath("$.content[1].id").value(b2.getId()));
    }

    @Test
    void searchByNameReturns200Filtered() throws Exception {
        brokerRepository.save(new Broker(
                "BR-001", "Best Broker", "a@test.ro", "0700000001", BrokerStatus.ACTIVE, new BigDecimal("0.10")
        ));
        brokerRepository.save(new Broker(
                "BR-002", "Other", "b@test.ro", "0700000002", BrokerStatus.ACTIVE, new BigDecimal("0.10")
        ));

        mockMvc.perform(get(BASE_PATH)
                        .param(PARAM_NAME, "best")
                        .param(PARAM_PAGE, PAGE_0)
                        .param(PARAM_SIZE, SIZE_20))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].brokerCode").value("BR-001"))
                .andExpect(jsonPath("$.content[0].name").value("Best Broker"));
    }

    @Test
    void searchByBrokerCodeReturnsSingle() throws Exception {
        Broker saved = brokerRepository.save(new Broker(
                "BR-XYZ", "XYZ", "x@test.ro", "0700000001", BrokerStatus.ACTIVE, new BigDecimal("0.10")
        ));

        mockMvc.perform(get(BASE_PATH)
                        .param(PARAM_CODE, "BR-XYZ")
                        .param(PARAM_PAGE, PAGE_0)
                        .param(PARAM_SIZE, SIZE_20))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(saved.getId()))
                .andExpect(jsonPath("$.content[0].brokerCode").value("BR-XYZ"));
    }

    @Test
    void searchByBrokerCodeNotFoundReturns404() throws Exception {
        mockMvc.perform(get(BASE_PATH)
                        .param(PARAM_CODE, "NOPE")
                        .param(PARAM_PAGE, PAGE_0)
                        .param(PARAM_SIZE, SIZE_20))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Broker not found for code: NOPE"))
                .andExpect(jsonPath("$.path").value(BASE_PATH));
    }

    @Test
    void createBrokerInvalidReturns400() throws Exception {
        BrokerCreateRequest badReq = new BrokerCreateRequest(
                " ",
                "x".repeat(201),
                "not-an-email",
                "y".repeat(41),
                new BigDecimal("0.10")
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
    void updateBrokerInvalidReturns400() throws Exception {
        Broker saved = brokerRepository.save(new Broker(
                "BR-001",
                "Best Broker",
                "broker@test.ro",
                "0722222222",
                BrokerStatus.ACTIVE,
                new BigDecimal("0.10")
        ));

        BrokerUpdateRequest badUpdate = new BrokerUpdateRequest(
                "x".repeat(201),
                "bad-email",
                "p".repeat(41),
                null,
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
    void getBrokerNotFoundReturns404() throws Exception {
        mockMvc.perform(get(BY_ID_PATH, MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Broker not found: " + MISSING_ID))
                .andExpect(jsonPath("$.path").value(BASE_PATH + "/" + MISSING_ID));
    }

    private String asJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
