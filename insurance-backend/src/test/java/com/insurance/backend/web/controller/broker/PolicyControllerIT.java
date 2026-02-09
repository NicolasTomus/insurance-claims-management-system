package com.insurance.backend.web.controller.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.backend.domain.broker.Broker;
import com.insurance.backend.domain.broker.BrokerStatus;
import com.insurance.backend.domain.building.Building;
import com.insurance.backend.domain.building.BuildingType;
import com.insurance.backend.domain.client.Client;
import com.insurance.backend.domain.client.ClientType;
import com.insurance.backend.domain.geography.City;
import com.insurance.backend.domain.geography.County;
import com.insurance.backend.domain.geography.Country;
import com.insurance.backend.domain.metadata.currency.Currency;
import com.insurance.backend.domain.metadata.fee.FeeConfiguration;
import com.insurance.backend.domain.metadata.fee.FeeType;
import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;
import com.insurance.backend.domain.metadata.risk.RiskLevel;
import com.insurance.backend.domain.policy.Policy;
import com.insurance.backend.domain.policy.PolicyStatus;
import com.insurance.backend.infrastructure.persistence.repository.broker.BrokerRepository;
import com.insurance.backend.infrastructure.persistence.repository.building.BuildingRepository;
import com.insurance.backend.infrastructure.persistence.repository.client.ClientRepository;
import com.insurance.backend.infrastructure.persistence.repository.geografy.CityRepository;
import com.insurance.backend.infrastructure.persistence.repository.geografy.CountyRepository;
import com.insurance.backend.infrastructure.persistence.repository.geografy.CountryRepository;
import com.insurance.backend.infrastructure.persistence.repository.metadata.currency.CurrencyRepository;
import com.insurance.backend.infrastructure.persistence.repository.metadata.fee.FeeConfigurationRepository;
import com.insurance.backend.infrastructure.persistence.repository.metadata.risk.RiskFactorConfigurationRepository;
import com.insurance.backend.infrastructure.persistence.repository.policy.PolicyRepository;
import com.insurance.backend.web.dto.policy.PolicyCancelRequest;
import com.insurance.backend.web.dto.policy.PolicyCreateDraftRequest;
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
import org.springframework.test.web.servlet.MvcResult;
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
class PolicyControllerIT {

    private static final String POSTGRES_IMAGE = "postgres:16";
    private static final String DB_NAME = "insurance_test";
    private static final String DB_USER = "test";
    private static final String DB_PASS = "test";

    private static final String BASE_PATH = "/api/brokers/policies";
    private static final String POLICY_BY_ID_PATH = BASE_PATH + "/{policyId}";
    private static final String POLICY_ACTIVATE_PATH = BASE_PATH + "/{policyId}/activate";
    private static final String POLICY_CANCEL_PATH = BASE_PATH + "/{policyId}/cancel";

    private static final String PARAM_PAGE = "page";
    private static final String PARAM_SIZE = "size";
    private static final String PAGE_0 = "0";
    private static final String SIZE_20 = "20";

    private static final String VALIDATION_FAILED = "Validation failed";
    private static final long MISSING_ID = 999999L;

    private static final String COUNTRY_NAME = "Romania";
    private static final String COUNTY_NAME = "Cluj";
    private static final String CITY_NAME = "Cluj-Napoca";

    private static final ClientType CLIENT_TYPE = ClientType.INDIVIDUAL;
    private static final String CLIENT_NAME = "Popescu Ion";
    private static final String CLIENT_CNP = "1234567890123";
    private static final String CLIENT_EMAIL = "ion@test.ro";
    private static final String CLIENT_PHONE = "0712345678";
    private static final String CLIENT_ADDRESS = "Str. Test 1";

    private static final String OWNER_NAME = "Owner";
    private static final String OTHER_NAME = "Other";
    private static final String OWNER_ID_NUMBER = "ID111";
    private static final String OTHER_ID_NUMBER = "ID222";
    private static final String OWNER_EMAIL = "o@test.ro";
    private static final String OTHER_EMAIL = "x@test.ro";
    private static final String OWNER_PHONE = "0700000001";
    private static final String OTHER_PHONE = "0700000002";

    private static final String BROKER_CODE = "BR-001";
    private static final String BROKER_NAME = "Best Broker";
    private static final String BROKER_EMAIL = "broker@test.ro";
    private static final String BROKER_PHONE = "0722222222";
    private static final BigDecimal BROKER_COMMISSION = new BigDecimal("0.10");

    private static final String CURRENCY_CODE_EUR = "EUR";
    private static final String CURRENCY_NAME_EUR = "Euro";
    private static final BigDecimal CURRENCY_RATE_EUR = new BigDecimal("1.00");
    private static final boolean CURRENCY_ACTIVE = true;

    private static final String BUILDING_ADDRESS = "Str. Marasesti 10";
    private static final String BUILDING_ADDRESS_ALT = "Addr 1";

    private static final int YEAR_2005 = 2005;
    private static final int YEAR_2000 = 2000;

    private static final BuildingType BUILDING_TYPE = BuildingType.RESIDENTIAL;
    private static final int FLOORS_2 = 2;

    private static final BigDecimal SURFACE_120_5 = new BigDecimal("120.5");
    private static final BigDecimal SURFACE_100 = new BigDecimal("100");
    private static final BigDecimal INSURED_100000 = new BigDecimal("100000");
    private static final BigDecimal INSURED_50000 = new BigDecimal("50000");

    private static final boolean FLOOD_FALSE = false;
    private static final boolean EARTHQUAKE_TRUE = true;
    private static final boolean EARTHQUAKE_FALSE = false;

    private static final String FEE_NAME = "AdminFee";
    private static final FeeType FEE_TYPE = FeeType.ADMIN_FEE;
    private static final BigDecimal FEE_PCT_10 = new BigDecimal("0.10");

    private static final BigDecimal RISK_PCT_05 = new BigDecimal("0.05");

    private static final BigDecimal BASE_PREMIUM_100 = new BigDecimal("100.00");
    private static final BigDecimal FINAL_PREMIUM_110 = new BigDecimal("110.00");

    private static final String POLICY_NUMBER_TEST = "POL-TEST-1";

    private static final String CANCEL_REASON = "Customer request";
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_CONFLICT = 409;

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
    @Autowired ObjectMapper objectMapper;

    @Autowired PolicyRepository policyRepository;
    @Autowired ClientRepository clientRepository;
    @Autowired BuildingRepository buildingRepository;
    @Autowired BrokerRepository brokerRepository;
    @Autowired CurrencyRepository currencyRepository;
    @Autowired FeeConfigurationRepository feeRepository;
    @Autowired RiskFactorConfigurationRepository riskRepository;

    @Autowired CityRepository cityRepository;
    @Autowired CountyRepository countyRepository;
    @Autowired CountryRepository countryRepository;

    @BeforeEach
    void cleanDb() {
        policyRepository.deleteAll();
        buildingRepository.deleteAll();

        cityRepository.deleteAll();
        countyRepository.deleteAll();
        countryRepository.deleteAll();

        clientRepository.deleteAll();
        brokerRepository.deleteAll();

        riskRepository.deleteAll();
        feeRepository.deleteAll();
        currencyRepository.deleteAll();
    }

    @Test
    void createDraftThenActivateThenGetThenCancelOk() throws Exception {
        Country country = countryRepository.save(new Country(COUNTRY_NAME));
        County county = countyRepository.save(new County(COUNTY_NAME, country));
        City city = cityRepository.save(new City(CITY_NAME, county));

        Client client = clientRepository.save(new Client(
                CLIENT_TYPE,
                CLIENT_NAME,
                CLIENT_CNP,
                CLIENT_EMAIL,
                CLIENT_PHONE,
                CLIENT_ADDRESS
        ));

        Broker broker = brokerRepository.save(new Broker(
                BROKER_CODE,
                BROKER_NAME,
                BROKER_EMAIL,
                BROKER_PHONE,
                BrokerStatus.ACTIVE,
                BROKER_COMMISSION
        ));

        Currency currency = currencyRepository.save(new Currency(
                CURRENCY_CODE_EUR,
                CURRENCY_NAME_EUR,
                CURRENCY_RATE_EUR,
                CURRENCY_ACTIVE
        ));

        Building building = buildingRepository.save(Building.builder()
                .owner(client)
                .address(BUILDING_ADDRESS)
                .city(city)
                .constructionYear(YEAR_2005)
                .buildingType(BUILDING_TYPE)
                .numberOfFloors(FLOORS_2)
                .surfaceArea(SURFACE_120_5)
                .insuredValue(INSURED_100000)
                .floodZone(FLOOD_FALSE)
                .earthquakeRiskZone(EARTHQUAKE_TRUE)
                .build());

        feeRepository.save(new FeeConfiguration(
                FEE_NAME,
                FEE_TYPE,
                FEE_PCT_10,
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(365),
                true
        ));

        riskRepository.save(new RiskFactorConfiguration(
                RiskLevel.COUNTRY,
                country.getId(),
                RISK_PCT_05,
                true
        ));

        PolicyCreateDraftRequest req = new PolicyCreateDraftRequest(
                client.getId(),
                building.getId(),
                broker.getId(),
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(30),
                BASE_PREMIUM_100,
                currency.getId(),
                null
        );

        MvcResult createRes = mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.policyNumber").exists())
                .andExpect(jsonPath("$.status").value(PolicyStatus.DRAFT.name()))
                .andExpect(jsonPath("$.currencyCode").value(CURRENCY_CODE_EUR))
                .andExpect(jsonPath("$.client.id").value(client.getId()))
                .andExpect(jsonPath("$.building.id").value(building.getId()))
                .andExpect(jsonPath("$.broker.id").value(broker.getId()))
                .andReturn();

        long policyId = objectMapper.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        Policy savedDraft = policyRepository.findById(policyId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(PolicyStatus.DRAFT, savedDraft.getStatus());

        mockMvc.perform(post(POLICY_ACTIVATE_PATH, policyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(policyId))
                .andExpect(jsonPath("$.status").value(PolicyStatus.ACTIVE.name()));

        Policy activated = policyRepository.findById(policyId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(PolicyStatus.ACTIVE, activated.getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(activated.getActivatedAt());

        mockMvc.perform(get(POLICY_BY_ID_PATH, policyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(policyId))
                .andExpect(jsonPath("$.status").value(PolicyStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.currencyCode").value(CURRENCY_CODE_EUR));

        PolicyCancelRequest cancelReq = new PolicyCancelRequest(CANCEL_REASON);
        mockMvc.perform(post(POLICY_CANCEL_PATH, policyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(cancelReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(PolicyStatus.CANCELLED.name()));

        Policy cancelled = policyRepository.findById(policyId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(PolicyStatus.CANCELLED, cancelled.getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(cancelled.getCancelledAt());
        org.junit.jupiter.api.Assertions.assertEquals(CANCEL_REASON, cancelled.getCancellationReason());
    }

    @Test
    void createDraftInvalidRequestReturns400() throws Exception {
        PolicyCreateDraftRequest badReq = new PolicyCreateDraftRequest(
                null,
                null,
                null,
                null,
                null,
                new BigDecimal("0.00"),
                null,
                null
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
    void getPolicyNotFoundReturns404() throws Exception {
        String expectedMessage = "Policy not found: " + MISSING_ID;
        String expectedPath = BASE_PATH + "/" + MISSING_ID;

        mockMvc.perform(get(POLICY_BY_ID_PATH, MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HTTP_NOT_FOUND))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.path").value(expectedPath));
    }

    @Test
    void createDraftBuildingNotOwnedReturns409() throws Exception {
        Country country = countryRepository.save(new Country(COUNTRY_NAME));
        County county = countyRepository.save(new County(COUNTY_NAME, country));
        City city = cityRepository.save(new City(CITY_NAME, county));

        Client owner = clientRepository.save(new Client(
                CLIENT_TYPE, OWNER_NAME, OWNER_ID_NUMBER, OWNER_EMAIL, OWNER_PHONE, null
        ));
        Client other = clientRepository.save(new Client(
                CLIENT_TYPE, OTHER_NAME, OTHER_ID_NUMBER, OTHER_EMAIL, OTHER_PHONE, null
        ));

        Broker broker = brokerRepository.save(new Broker(
                BROKER_CODE, BROKER_NAME, BROKER_EMAIL, BROKER_PHONE, BrokerStatus.ACTIVE, BROKER_COMMISSION
        ));
        Currency currency = currencyRepository.save(new Currency(CURRENCY_CODE_EUR, CURRENCY_NAME_EUR, CURRENCY_RATE_EUR, CURRENCY_ACTIVE));

        Building building = buildingRepository.save(Building.builder()
                .owner(owner)
                .address(BUILDING_ADDRESS_ALT)
                .city(city)
                .constructionYear(YEAR_2000)
                .buildingType(BUILDING_TYPE)
                .numberOfFloors(FLOORS_2)
                .surfaceArea(SURFACE_100)
                .insuredValue(INSURED_50000)
                .floodZone(FLOOD_FALSE)
                .earthquakeRiskZone(EARTHQUAKE_FALSE)
                .build());

        PolicyCreateDraftRequest req = new PolicyCreateDraftRequest(
                other.getId(),
                building.getId(),
                broker.getId(),
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(30),
                BASE_PREMIUM_100,
                currency.getId(),
                null
        );

        String expectedMessage = "Building " + building.getId() + " is not owned by client " + other.getId();

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(HTTP_CONFLICT))
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    @Test
    void createDraftBrokerInactiveReturns409() throws Exception {
        Country country = countryRepository.save(new Country(COUNTRY_NAME));
        County county = countyRepository.save(new County(COUNTY_NAME, country));
        City city = cityRepository.save(new City(CITY_NAME, county));

        Client client = clientRepository.save(new Client(
                CLIENT_TYPE, CLIENT_NAME, CLIENT_CNP, CLIENT_EMAIL, CLIENT_PHONE, null
        ));
        Broker broker = brokerRepository.save(new Broker(
                BROKER_CODE, BROKER_NAME, BROKER_EMAIL, BROKER_PHONE, BrokerStatus.INACTIVE, BROKER_COMMISSION
        ));
        Currency currency = currencyRepository.save(new Currency(CURRENCY_CODE_EUR, CURRENCY_NAME_EUR, CURRENCY_RATE_EUR, CURRENCY_ACTIVE));

        Building building = buildingRepository.save(Building.builder()
                .owner(client)
                .address(BUILDING_ADDRESS_ALT)
                .city(city)
                .constructionYear(YEAR_2000)
                .buildingType(BUILDING_TYPE)
                .numberOfFloors(FLOORS_2)
                .surfaceArea(SURFACE_100)
                .insuredValue(INSURED_50000)
                .floodZone(FLOOD_FALSE)
                .earthquakeRiskZone(EARTHQUAKE_FALSE)
                .build());

        PolicyCreateDraftRequest req = new PolicyCreateDraftRequest(
                client.getId(),
                building.getId(),
                broker.getId(),
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(30),
                BASE_PREMIUM_100,
                currency.getId(),
                null
        );

        String expectedMessage = "Broker is inactive: " + broker.getId();

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(HTTP_CONFLICT))
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    @Test
    void activateNotDraftReturns409() throws Exception {
        Country country = countryRepository.save(new Country(COUNTRY_NAME));
        County county = countyRepository.save(new County(COUNTY_NAME, country));
        City city = cityRepository.save(new City(CITY_NAME, county));

        Client client = clientRepository.save(new Client(
                CLIENT_TYPE, CLIENT_NAME, CLIENT_CNP, CLIENT_EMAIL, CLIENT_PHONE, null
        ));
        Broker broker = brokerRepository.save(new Broker(
                BROKER_CODE, BROKER_NAME, BROKER_EMAIL, BROKER_PHONE, BrokerStatus.ACTIVE, BROKER_COMMISSION
        ));
        Currency currency = currencyRepository.save(new Currency(CURRENCY_CODE_EUR, CURRENCY_NAME_EUR, CURRENCY_RATE_EUR, CURRENCY_ACTIVE));

        Building building = buildingRepository.save(Building.builder()
                .owner(client)
                .address(BUILDING_ADDRESS_ALT)
                .city(city)
                .constructionYear(YEAR_2000)
                .buildingType(BUILDING_TYPE)
                .numberOfFloors(FLOORS_2)
                .surfaceArea(SURFACE_100)
                .insuredValue(INSURED_50000)
                .floodZone(FLOOD_FALSE)
                .earthquakeRiskZone(EARTHQUAKE_FALSE)
                .build());

        Policy policy = new Policy();
        policy.setPolicyNumber(POLICY_NUMBER_TEST);
        policy.setClient(client);
        policy.setBuilding(building);
        policy.setBroker(broker);
        policy.setCurrency(currency);
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setStartDate(LocalDate.now().plusDays(5));
        policy.setEndDate(LocalDate.now().plusDays(30));
        policy.setBasePremiumAmount(BASE_PREMIUM_100);
        policy.setFinalPremiumAmount(FINAL_PREMIUM_110);

        Policy saved = policyRepository.save(policy);

        mockMvc.perform(post(POLICY_ACTIVATE_PATH, saved.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(HTTP_CONFLICT))
                .andExpect(jsonPath("$.message").value("Only DRAFT policies can be activated"));
    }

    @Test
    void searchNoFiltersReturns200Page() throws Exception {
        Country country = countryRepository.save(new Country(COUNTRY_NAME));
        County county = countyRepository.save(new County(COUNTY_NAME, country));
        City city = cityRepository.save(new City(CITY_NAME, county));

        Client client = clientRepository.save(new Client(
                CLIENT_TYPE, CLIENT_NAME, CLIENT_CNP, CLIENT_EMAIL, CLIENT_PHONE, null
        ));
        Broker broker = brokerRepository.save(new Broker(
                BROKER_CODE, BROKER_NAME, BROKER_EMAIL, BROKER_PHONE, BrokerStatus.ACTIVE, BROKER_COMMISSION
        ));
        Currency currency = currencyRepository.save(new Currency(CURRENCY_CODE_EUR, CURRENCY_NAME_EUR, CURRENCY_RATE_EUR, CURRENCY_ACTIVE));

        Building building = buildingRepository.save(Building.builder()
                .owner(client)
                .address(BUILDING_ADDRESS_ALT)
                .city(city)
                .constructionYear(YEAR_2000)
                .buildingType(BUILDING_TYPE)
                .numberOfFloors(FLOORS_2)
                .surfaceArea(SURFACE_100)
                .insuredValue(INSURED_50000)
                .floodZone(FLOOD_FALSE)
                .earthquakeRiskZone(EARTHQUAKE_FALSE)
                .build());

        PolicyCreateDraftRequest req = new PolicyCreateDraftRequest(
                client.getId(),
                building.getId(),
                broker.getId(),
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(30),
                BASE_PREMIUM_100,
                currency.getId(),
                null
        );

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_PATH)
                        .param(PARAM_PAGE, PAGE_0)
                        .param(PARAM_SIZE, SIZE_20))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value(PolicyStatus.DRAFT.name()));
    }

    private String asJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
