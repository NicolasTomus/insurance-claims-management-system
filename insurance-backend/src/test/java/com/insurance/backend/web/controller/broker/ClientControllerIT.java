package com.insurance.backend.web.controller.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.backend.domain.client.Client;
import com.insurance.backend.domain.client.ClientType;
import com.insurance.backend.infrastructure.persistence.repository.client.ClientRepository;
import com.insurance.backend.web.dto.client.ClientCreateRequest;
import com.insurance.backend.web.dto.client.ClientUpdateRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientControllerIT {

    private static final String POSTGRES_IMAGE = "postgres:16";
    private static final String DB_NAME = "insurance_test";
    private static final String DB_USER = "test";
    private static final String DB_PASS = "test";

    private static final String BASE_PATH = "/api/brokers/clients";
    private static final String CLIENT_BY_ID_PATH = BASE_PATH + "/{clientId}";

    private static final String PARAM_PAGE = "page";
    private static final String PARAM_SIZE = "size";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_IDENTIFIER = "identifier";

    private static final String PAGE_0 = "0";
    private static final String SIZE_20 = "20";

    private static final String POPESCU_ION = "Popescu Ion";
    private static final String IDENTIFIER_1234567890123 = "1234567890123";
    private static final String ION_TEST_RO = "ion@test.ro";
    private static final String PHONE_0712345678 = "0712345678";
    private static final String ADDRESS_STR_TEST_1 = "Str. Test 1";

    private static final String UPDATED_NAME = "Popescu Ion Updated";
    private static final String UPDATED_EMAIL = "ion.updated@test.ro";
    private static final String UPDATED_PHONE = "0799999999";
    private static final String UPDATED_ADDRESS = "Str. Noua 10";

    private static final String ALPHA = "Alpha";
    private static final String BETA_SRL = "Beta SRL";
    private static final String ID_111 = "111";
    private static final String ID_222 = "222";
    private static final String A_TEST_RO = "a@test.ro";
    private static final String B_TEST_RO = "b@test.ro";
    private static final String PHONE_0700000001 = "0700000001";
    private static final String PHONE_0700000002 = "0700000002";
    private static final String ADDR_1 = "Addr 1";
    private static final String ADDR_2 = "Addr 2";

    private static final String IONESCU_MIHAI = "Ionescu Mihai";
    private static final String ID_123 = "123";
    private static final String ID_456 = "456";
    private static final String MIHAI_TEST_RO = "mihai@test.ro";
    private static final String NAME_FILTER_POPESCU = "popescu";

    private static final String NOT_AN_EMAIL = "not-an-email";
    private static final String BAD_EMAIL = "bad-email";
    private static final String NOPE = "NOPE";

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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @BeforeEach
    void cleanDb() {
        clientRepository.deleteAll();
    }

    @Test
    void createClientOkReturns201() throws Exception {
        ClientCreateRequest req = createValidCreateRequest();

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.clientType").value(ClientType.INDIVIDUAL.name()))
                .andExpect(jsonPath("$.name").value(POPESCU_ION))
                .andExpect(jsonPath("$.identificationNumber").value(IDENTIFIER_1234567890123))
                .andExpect(jsonPath("$.buildings").exists());
    }

    @Test
    void getClientOkReturns200() throws Exception {
        Client saved = clientRepository.save(createClient(
                ClientType.INDIVIDUAL,
                POPESCU_ION,
                IDENTIFIER_1234567890123,
                ION_TEST_RO,
                PHONE_0712345678,
                ADDRESS_STR_TEST_1
        ));

        mockMvc.perform(get(CLIENT_BY_ID_PATH, saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.identificationNumber").value(IDENTIFIER_1234567890123));
    }

    @Test
    void updateClientOkReturns200() throws Exception {
        Client saved = clientRepository.save(createClient(
                ClientType.INDIVIDUAL,
                POPESCU_ION,
                IDENTIFIER_1234567890123,
                ION_TEST_RO,
                PHONE_0712345678,
                ADDRESS_STR_TEST_1
        ));

        ClientUpdateRequest update = createValidUpdateRequest();

        mockMvc.perform(put(CLIENT_BY_ID_PATH, saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value(UPDATED_NAME))
                .andExpect(jsonPath("$.email").value(UPDATED_EMAIL));
    }

    @Test
    void searchClientsNoFiltersReturns200Page() throws Exception {
        Client c1 = clientRepository.save(createClient(
                ClientType.INDIVIDUAL, ALPHA, ID_111, A_TEST_RO, PHONE_0700000001, ADDR_1
        ));
        Client c2 = clientRepository.save(createClient(
                ClientType.COMPANY, BETA_SRL, ID_222, B_TEST_RO, PHONE_0700000002, ADDR_2
        ));

        mockMvc.perform(get(BASE_PATH)
                        .param(PARAM_PAGE, PAGE_0)
                        .param(PARAM_SIZE, SIZE_20))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(c1.getId()))
                .andExpect(jsonPath("$.content[0].identificationNumber").value(ID_111))
                .andExpect(jsonPath("$.content[1].id").value(c2.getId()));
    }

    @Test
    void searchClientsByNameReturns200() throws Exception {
        clientRepository.save(createClient(
                ClientType.INDIVIDUAL, POPESCU_ION, ID_123, ION_TEST_RO, PHONE_0700000001, ADDR_1
        ));
        clientRepository.save(createClient(
                ClientType.INDIVIDUAL, IONESCU_MIHAI, ID_456, MIHAI_TEST_RO, PHONE_0700000002, ADDR_2
        ));

        mockMvc.perform(get(BASE_PATH)
                        .param(PARAM_NAME, NAME_FILTER_POPESCU)
                        .param(PARAM_PAGE, PAGE_0)
                        .param(PARAM_SIZE, SIZE_20))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value(POPESCU_ION))
                .andExpect(jsonPath("$.content[0].identificationNumber").value(ID_123));
    }

    @Test
    void createClientInvalidReturns400() throws Exception {
        ClientCreateRequest badReq = new ClientCreateRequest(
                null,
                "   ",
                "",
                NOT_AN_EMAIL,
                "x".repeat(41),
                "y".repeat(301)
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
    void updateClientInvalidReturns400() throws Exception {
        Client saved = clientRepository.save(createClient(
                ClientType.INDIVIDUAL, POPESCU_ION, ID_123, ION_TEST_RO, PHONE_0700000001, ADDR_1
        ));

        ClientUpdateRequest badUpdate = new ClientUpdateRequest(
                "a".repeat(201),
                BAD_EMAIL,
                "p".repeat(41),
                "z".repeat(301),
                "b".repeat(33)
        );

        mockMvc.perform(put(CLIENT_BY_ID_PATH, saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(badUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_FAILED))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void getClientNotFoundReturns404() throws Exception {
        mockMvc.perform(get(CLIENT_BY_ID_PATH, MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Client not found: " + MISSING_ID))
                .andExpect(jsonPath("$.path").value(BASE_PATH + "/" + MISSING_ID));
    }

    @Test
    void searchClientsIdentifierNotFoundReturns404() throws Exception {
        mockMvc.perform(get(BASE_PATH)
                        .param(PARAM_IDENTIFIER, NOPE)
                        .param(PARAM_PAGE, PAGE_0)
                        .param(PARAM_SIZE, SIZE_20))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Client not found for identifier: " + NOPE))
                .andExpect(jsonPath("$.path").value(BASE_PATH));
    }

    private ClientCreateRequest createValidCreateRequest() {
        return new ClientCreateRequest(
                ClientType.INDIVIDUAL,
                POPESCU_ION,
                IDENTIFIER_1234567890123,
                ION_TEST_RO,
                PHONE_0712345678,
                ADDRESS_STR_TEST_1
        );
    }

    private ClientUpdateRequest createValidUpdateRequest() {
        return new ClientUpdateRequest(
                UPDATED_NAME,
                UPDATED_EMAIL,
                UPDATED_PHONE,
                UPDATED_ADDRESS,
                null
        );
    }

    private static Client createClient(
            ClientType type,
            String name,
            String identificationNumber,
            String email,
            String phone,
            String address
    ) {
        return new Client(type, name, identificationNumber, email, phone, address);
    }

    private String asJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
