package com.insurance.backend.web.controller.admin;

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
import com.insurance.backend.domain.policy.Policy;
import com.insurance.backend.domain.policy.PolicyStatus;
import com.insurance.backend.infrastructure.persistence.repository.broker.BrokerRepository;
import com.insurance.backend.infrastructure.persistence.repository.building.BuildingRepository;
import com.insurance.backend.infrastructure.persistence.repository.client.ClientRepository;
import com.insurance.backend.infrastructure.persistence.repository.geografy.CityRepository;
import com.insurance.backend.infrastructure.persistence.repository.geografy.CountryRepository;
import com.insurance.backend.infrastructure.persistence.repository.geografy.CountyRepository;
import com.insurance.backend.infrastructure.persistence.repository.metadata.currency.CurrencyRepository;
import com.insurance.backend.infrastructure.persistence.repository.policy.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    private static final String DB_PASSWORD = "test";

    private static final String REPORT_BY_BROKER_URL = "/api/admin/reports/policies/by-broker";

    private static final String PARAM_START_DATE = "startDate";
    private static final String PARAM_END_DATE = "endDate";
    private static final String DATE_2026_01_01 = "2026-01-01";
    private static final String DATE_2026_12_31 = "2026-12-31";

    private static final String COUNTRY_NAME = "Romania";
    private static final String COUNTY_NAME = "Cluj";
    private static final String CITY_NAME = "Cluj-Napoca";

    private static final ClientType CLIENT_TYPE = ClientType.INDIVIDUAL;
    private static final String CLIENT_NAME = "Popescu Ion";
    private static final String CLIENT_CNP = "1234567890123";
    private static final String CLIENT_EMAIL = "ion@test.ro";
    private static final String CLIENT_PHONE = "0712345678";
    private static final String CLIENT_ADDRESS = "Str. Test 1";

    private static final String BUILDING_ADDRESS = "Str. X 1";
    private static final int BUILDING_YEAR = 2005;
    private static final BuildingType BUILDING_TYPE = BuildingType.RESIDENTIAL;
    private static final int BUILDING_FLOORS = 2;
    private static final BigDecimal BUILDING_INSURED_VALUE = new BigDecimal("100000");
    private static final BigDecimal BUILDING_SURFACE_AREA = new BigDecimal("120.5");
    private static final boolean BUILDING_FLOOD_ZONE = false;
    private static final boolean BUILDING_EARTHQUAKE_ZONE = false;

    private static final String BROKER_CODE_1 = "BR-1";
    private static final String BROKER_CODE_2 = "BR-2";
    private static final String BROKER_NAME_1 = "B1";
    private static final String BROKER_NAME_2 = "B2";
    private static final String BROKER_EMAIL_1 = "b1@test.ro";
    private static final String BROKER_EMAIL_2 = "b2@test.ro";
    private static final String BROKER_PHONE_1 = "0700";
    private static final String BROKER_PHONE_2 = "0701";
    private static final BrokerStatus BROKER_STATUS = BrokerStatus.ACTIVE;
    private static final BigDecimal BROKER_COMMISSION = new BigDecimal("0.10");

    private static final String CURRENCY_CODE_EUR = "EUR";
    private static final String CURRENCY_NAME_EUR = "Euro";
    private static final BigDecimal CURRENCY_RATE_EUR = new BigDecimal("1.00");

    private static final String CURRENCY_CODE_USD = "USD";
    private static final String CURRENCY_NAME_USD = "Dollar";
    private static final BigDecimal CURRENCY_RATE_USD = new BigDecimal("2.00");

    private static final boolean CURRENCY_ACTIVE = true;

    private static final String POLICY_NUMBER_1 = "P-1";
    private static final String POLICY_NUMBER_2 = "P-2";

    private static final LocalDate POLICY1_START = LocalDate.of(2026, 1, 15);
    private static final LocalDate POLICY1_END = LocalDate.of(2026, 2, 1);

    private static final LocalDate POLICY2_START = LocalDate.of(2026, 1, 20);
    private static final LocalDate POLICY2_END = LocalDate.of(2026, 2, 5);

    private static final BigDecimal POLICY_BASE_PREMIUM = new BigDecimal("5.00");
    private static final BigDecimal POLICY_FINAL_PREMIUM_10 = new BigDecimal("10.00");
    private static final BigDecimal POLICY_FINAL_PREMIUM_7 = new BigDecimal("7.00");

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USER)
            .withPassword(DB_PASSWORD);

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired MockMvc mockMvc;

    @Autowired PolicyRepository policyRepository;
    @Autowired BrokerRepository brokerRepository;
    @Autowired ClientRepository clientRepository;
    @Autowired BuildingRepository buildingRepository;
    @Autowired CurrencyRepository currencyRepository;

    @Autowired CityRepository cityRepository;
    @Autowired CountyRepository countyRepository;
    @Autowired CountryRepository countryRepository;

    @BeforeEach
    void cleanDb() {
        policyRepository.deleteAll();
        buildingRepository.deleteAll();
        clientRepository.deleteAll();
        brokerRepository.deleteAll();
        currencyRepository.deleteAll();
        cityRepository.deleteAll();
        countyRepository.deleteAll();
        countryRepository.deleteAll();
    }

    @Test
    void byBrokerOkReturnsAggregatesSortedDesc() throws Exception {
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

        Building building = buildingRepository.save(Building.builder()
                .owner(client)
                .address(BUILDING_ADDRESS)
                .city(city)
                .constructionYear(BUILDING_YEAR)
                .buildingType(BUILDING_TYPE)
                .numberOfFloors(BUILDING_FLOORS)
                .insuredValue(BUILDING_INSURED_VALUE)
                .surfaceArea(BUILDING_SURFACE_AREA)
                .floodZone(BUILDING_FLOOD_ZONE)
                .earthquakeRiskZone(BUILDING_EARTHQUAKE_ZONE)
                .build()
        );

        Broker b1 = brokerRepository.save(new Broker(
                BROKER_CODE_1, BROKER_NAME_1, BROKER_EMAIL_1, BROKER_PHONE_1, BROKER_STATUS, BROKER_COMMISSION
        ));
        Broker b2 = brokerRepository.save(new Broker(
                BROKER_CODE_2, BROKER_NAME_2, BROKER_EMAIL_2, BROKER_PHONE_2, BROKER_STATUS, BROKER_COMMISSION
        ));

        Currency c1 = currencyRepository.save(new Currency(CURRENCY_CODE_EUR, CURRENCY_NAME_EUR, CURRENCY_RATE_EUR, CURRENCY_ACTIVE));
        Currency c2 = currencyRepository.save(new Currency(CURRENCY_CODE_USD, CURRENCY_NAME_USD, CURRENCY_RATE_USD, CURRENCY_ACTIVE));

        policyRepository.save(createPolicy(POLICY_NUMBER_1, client, building, b1, c1,
                POLICY1_START, POLICY1_END,
                POLICY_BASE_PREMIUM, POLICY_FINAL_PREMIUM_10));

        policyRepository.save(createPolicy(POLICY_NUMBER_2, client, building, b2, c2,
                POLICY2_START, POLICY2_END,
                POLICY_BASE_PREMIUM, POLICY_FINAL_PREMIUM_7));

        mockMvc.perform(get(REPORT_BY_BROKER_URL)
                        .param(PARAM_START_DATE, DATE_2026_01_01)
                        .param(PARAM_END_DATE, DATE_2026_12_31))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].key").value(b2.getId().toString()))
                .andExpect(jsonPath("$[0].count").value(1))
                .andExpect(jsonPath("$[1].key").value(b1.getId().toString()))
                .andExpect(jsonPath("$[1].count").value(1));
    }

    private Policy createPolicy(
            String number,
            Client client,
            Building building,
            Broker broker,
            Currency currency,
            LocalDate start,
            LocalDate end,
            BigDecimal base,
            BigDecimal fin
    ) {
        Policy policy = new Policy();
        policy.setPolicyNumber(number);
        policy.setClient(client);
        policy.setBuilding(building);
        policy.setBroker(broker);
        policy.setCurrency(currency);
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setStartDate(start);
        policy.setEndDate(end);
        policy.setBasePremiumAmount(base);
        policy.setFinalPremiumAmount(fin);
        return policy;
    }
}
