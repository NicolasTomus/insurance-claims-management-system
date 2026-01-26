package com.insurance.backend.web.controller.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.backend.domain.client.Client;
import com.insurance.backend.domain.client.ClientType;
import com.insurance.backend.infrastructure.persistence.repository.ClientRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientControllerIT {

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
    @Autowired ClientRepository clientRepository;

    @BeforeEach
    void cleanDb() {
        clientRepository.deleteAll();
    }

    // /api/brokers/clients
    @Test
    void createClient_ok_returns201() throws Exception {
        ClientCreateRequest req = new ClientCreateRequest(
                ClientType.INDIVIDUAL,
                "Popescu Ion",
                "1234567890123",
                "ion@test.ro",
                "0712345678",
                "Str. Test 1"
        );

        mockMvc.perform(post("/api/brokers/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.clientType").value("INDIVIDUAL"))
                .andExpect(jsonPath("$.name").value("Popescu Ion"))
                .andExpect(jsonPath("$.identificationNumber").value("1234567890123"))
                .andExpect(jsonPath("$.buildings").exists());
    }

    // /api/brokers/clients/1
    @Test
    void getClient_ok_returns200() throws Exception {
        Client saved = clientRepository.save(new Client(
                ClientType.INDIVIDUAL,
                "Popescu Ion",
                "1234567890123",
                "ion@test.ro",
                "0712345678",
                "Str. Test 1"
        ));

        mockMvc.perform(get("/api/brokers/clients/{clientId}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.identificationNumber").value("1234567890123"));
    }

    // /api/brokers/clients/1
    @Test
    void updateClient_ok_returns200() throws Exception {
        Client saved = clientRepository.save(new Client(
                ClientType.INDIVIDUAL,
                "Popescu Ion",
                "1234567890123",
                "ion@test.ro",
                "0712345678",
                "Str. Test 1"
        ));

        ClientUpdateRequest update = new ClientUpdateRequest(
                "Popescu Ion Updated",
                "ion.updated@test.ro",
                "0799999999",
                "Str. Noua 10",
                null
        );

        mockMvc.perform(put("/api/brokers/clients/{clientId}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Popescu Ion Updated"))
                .andExpect(jsonPath("$.email").value("ion.updated@test.ro"));
    }

    // /api/brokers/clients
    @Test
    void searchClients_noFilters_returns200_page() throws Exception {
        Client c1 = clientRepository.save(new Client(
                ClientType.INDIVIDUAL, "Alpha", "111", "a@test.ro", "0700000001", "Addr 1"
        ));
        Client c2 = clientRepository.save(new Client(
                ClientType.COMPANY, "Beta SRL", "222", "b@test.ro", "0700000002", "Addr 2"
        ));

        mockMvc.perform(get("/api/brokers/clients")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(c2.getId()))
                .andExpect(jsonPath("$.content[0].identificationNumber").value("222"))
                .andExpect(jsonPath("$.content[1].id").value(c1.getId()));
    }

    // /api/brokers/clients?name=Popescu Ion
    @Test
    void searchClients_byName_returns200() throws Exception {
        clientRepository.save(new Client(
                ClientType.INDIVIDUAL, "Popescu Ion", "123", "ion@test.ro", "0700000001", "Addr 1"
        ));
        clientRepository.save(new Client(
                ClientType.INDIVIDUAL, "Ionescu Mihai", "456", "mihai@test.ro", "0700000002", "Addr 2"
        ));

        mockMvc.perform(get("/api/brokers/clients")
                        .param("name", "popescu")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Popescu Ion"))
                .andExpect(jsonPath("$.content[0].identificationNumber").value("123"));
    }

    @Test
    void createClient_invalid_returns400() throws Exception {
        ClientCreateRequest badReq = new ClientCreateRequest(
                null,
                "   ",
                "",
                "not-an-email",
                "x".repeat(41),
                "y".repeat(301)
        );

        mockMvc.perform(post("/api/brokers/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void updateClient_invalid_returns400() throws Exception {
        Client saved = clientRepository.save(new Client(
                ClientType.INDIVIDUAL, "Popescu Ion", "123", "ion@test.ro", "0700000001", "Addr 1"
        ));

        ClientUpdateRequest badUpdate = new ClientUpdateRequest(
                "a".repeat(201),
                "bad-email",
                "p".repeat(41),
                "z".repeat(301),
                "b".repeat(33)
        );

        mockMvc.perform(put("/api/brokers/clients/{clientId}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void getClient_notFound_returns404() throws Exception {
        long missingId = 999999L;

        mockMvc.perform(get("/api/brokers/clients/{clientId}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Client not found: " + missingId))
                .andExpect(jsonPath("$.path").value("/api/brokers/clients/" + missingId));
    }

    @Test
    void searchClients_identifierNotFound_returns404() throws Exception {
        mockMvc.perform(get("/api/brokers/clients")
                        .param("identifier", "NOPE")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Client not found for identifier: " + "NOPE"))
                .andExpect(jsonPath("$.path").value("/api/brokers/clients"));
    }

}
