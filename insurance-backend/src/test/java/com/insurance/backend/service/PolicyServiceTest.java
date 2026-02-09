package com.insurance.backend.service;

import com.insurance.backend.domain.broker.Broker;
import com.insurance.backend.domain.broker.BrokerStatus;
import com.insurance.backend.domain.building.Building;
import com.insurance.backend.domain.client.Client;
import com.insurance.backend.domain.metadata.currency.Currency;
import com.insurance.backend.domain.metadata.fee.FeeConfiguration;
import com.insurance.backend.domain.metadata.risk.RiskLevel;
import com.insurance.backend.domain.policy.Policy;
import com.insurance.backend.domain.policy.PolicyStatus;
import com.insurance.backend.infrastructure.persistence.repository.broker.BrokerRepository;
import com.insurance.backend.infrastructure.persistence.repository.building.BuildingRepository;
import com.insurance.backend.infrastructure.persistence.repository.client.ClientRepository;
import com.insurance.backend.infrastructure.persistence.repository.metadata.currency.CurrencyRepository;
import com.insurance.backend.infrastructure.persistence.repository.metadata.fee.FeeConfigurationRepository;
import com.insurance.backend.infrastructure.persistence.repository.metadata.risk.RiskFactorConfigurationRepository;
import com.insurance.backend.infrastructure.persistence.repository.policy.PolicyRepository;
import com.insurance.backend.web.dto.policy.PolicyCancelRequest;
import com.insurance.backend.web.dto.policy.PolicyCreateDraftRequest;
import com.insurance.backend.web.dto.policy.PolicyDetailsResponse;
import com.insurance.backend.web.dto.policy.PolicySummaryResponse;
import com.insurance.backend.web.exception.ConflictException;
import com.insurance.backend.web.exception.NotFoundException;
import com.insurance.backend.web.mapper.PolicyMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    private static final long POLICY_ID = 100L;

    private static final long CLIENT_ID = 10L;
    private static final long BUILDING_ID = 20L;
    private static final long BROKER_ID = 30L;
    private static final long CURRENCY_ID = 40L;

    private static final long FEE_ID_1 = 1L;
    private static final long FEE_ID_2 = 2L;

    private static final int PAGE_NUMBER = 0;
    private static final int PAGE_SIZE = 10;
    private static final long TOTAL_ELEMENTS = 2L;

    private static final LocalDate START_DATE_DRAFT = LocalDate.of(2026, 2, 10);
    private static final LocalDate END_DATE_DRAFT = LocalDate.of(2026, 3, 10);

    private static final BigDecimal BASE_PREMIUM = new BigDecimal("100.00");
    private static final BigDecimal FINAL_PREMIUM_110 = new BigDecimal("110.00");
    private static final BigDecimal FINAL_PREMIUM_130 = new BigDecimal("130.00");

    private static final String CANCEL_REASON_SHORT = "x";
    private static final String CANCEL_REASON_CUSTOMER_REQUEST = "Customer request";

    private static Pageable defaultPageable() {
        return PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
    }

    private static PolicyCreateDraftRequest draftRequest(List<Long> feeIds) {
        return new PolicyCreateDraftRequest(
                CLIENT_ID,
                BUILDING_ID,
                BROKER_ID,
                START_DATE_DRAFT,
                END_DATE_DRAFT,
                BASE_PREMIUM,
                CURRENCY_ID,
                feeIds
        );
    }

    @Mock private PolicyRepository policyRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private BuildingRepository buildingRepository;
    @Mock private BrokerRepository brokerRepository;
    @Mock private CurrencyRepository currencyRepository;
    @Mock private FeeConfigurationRepository feeRepository;
    @Mock private RiskFactorConfigurationRepository riskRepository;
    @Mock private PolicyMapper policyMapper;
    @Mock private PolicyCalculationService calculationService;

    @InjectMocks
    private PolicyService policyService;

    @Test
    void createDraftShouldThrowNotFoundWhenClientMissing() {
        PolicyCreateDraftRequest req = draftRequest(null);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> policyService.createDraft(req));

        verify(clientRepository).findById(CLIENT_ID);
        verifyNoMoreInteractions(clientRepository);
        verifyNoInteractions(buildingRepository, brokerRepository, currencyRepository, feeRepository, riskRepository, policyMapper, calculationService, policyRepository);
    }

    @Test
    void createDraftShouldThrowConflictWhenBuildingNotOwnedByClient() {
        PolicyCreateDraftRequest req = draftRequest(null);
        Client client = mock(Client.class);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(buildingRepository.findByIdAndOwnerId(BUILDING_ID, CLIENT_ID)).thenReturn(Optional.empty());

        assertThrows(ConflictException.class, () -> policyService.createDraft(req));

        verify(clientRepository).findById(CLIENT_ID);
        verify(buildingRepository).findByIdAndOwnerId(BUILDING_ID, CLIENT_ID);
        verifyNoInteractions(brokerRepository, currencyRepository, feeRepository, riskRepository, policyMapper, calculationService, policyRepository);
    }

    @Test
    void createDraftShouldThrowNotFoundWhenBrokerMissing() {
        PolicyCreateDraftRequest req = draftRequest(null);
        Client client = mock(Client.class);
        Building building = mock(Building.class);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(buildingRepository.findByIdAndOwnerId(BUILDING_ID, CLIENT_ID)).thenReturn(Optional.of(building));
        when(brokerRepository.findById(BROKER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> policyService.createDraft(req));

        verify(brokerRepository).findById(BROKER_ID);
        verifyNoInteractions(currencyRepository, feeRepository, riskRepository, policyMapper, calculationService, policyRepository);
    }

    @Test
    void createDraftShouldThrowConflictWhenBrokerInactive() {
        PolicyCreateDraftRequest req = draftRequest(null);
        Client client = mock(Client.class);
        Building building = mock(Building.class);
        Broker broker = mock(Broker.class);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(buildingRepository.findByIdAndOwnerId(BUILDING_ID, CLIENT_ID)).thenReturn(Optional.of(building));
        when(brokerRepository.findById(BROKER_ID)).thenReturn(Optional.of(broker));
        when(broker.getStatus()).thenReturn(BrokerStatus.INACTIVE);
        when(broker.getId()).thenReturn(BROKER_ID);

        assertThrows(ConflictException.class, () -> policyService.createDraft(req));

        verify(broker).getStatus();
        verifyNoInteractions(currencyRepository, feeRepository, riskRepository, policyMapper, calculationService, policyRepository);
    }

    @Test
    void createDraftShouldThrowNotFoundWhenCurrencyMissing() {
        PolicyCreateDraftRequest req = draftRequest(null);
        Client client = mock(Client.class);
        Building building = mock(Building.class);
        Broker broker = mock(Broker.class);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(buildingRepository.findByIdAndOwnerId(BUILDING_ID, CLIENT_ID)).thenReturn(Optional.of(building));
        when(brokerRepository.findById(BROKER_ID)).thenReturn(Optional.of(broker));
        when(broker.getStatus()).thenReturn(BrokerStatus.ACTIVE);

        when(currencyRepository.findById(CURRENCY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> policyService.createDraft(req));

        verify(currencyRepository).findById(CURRENCY_ID);
        verifyNoInteractions(feeRepository, riskRepository, policyMapper, calculationService, policyRepository);
    }

    @Test
    void createDraftShouldUseFeeIdsWhenProvidedAndReturnDetails() {
        List<Long> feeIds = List.of(FEE_ID_1, FEE_ID_2);
        PolicyCreateDraftRequest req = draftRequest(feeIds);

        Client client = mock(Client.class);
        Building building = mock(Building.class);
        Broker broker = mock(Broker.class);
        Currency currency = mock(Currency.class);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(buildingRepository.findByIdAndOwnerId(BUILDING_ID, CLIENT_ID)).thenReturn(Optional.of(building));
        when(brokerRepository.findById(BROKER_ID)).thenReturn(Optional.of(broker));
        when(broker.getStatus()).thenReturn(BrokerStatus.ACTIVE);
        when(currencyRepository.findById(CURRENCY_ID)).thenReturn(Optional.of(currency));

        List<FeeConfiguration> fees = List.of(mock(FeeConfiguration.class));
        when(feeRepository.findAllByIdIn(feeIds)).thenReturn(fees);

        when(building.getCity()).thenReturn(null);
        when(building.getBuildingType()).thenReturn(null);

        Policy policy = new Policy();
        policy.setBasePremiumAmount(req.basePremiumAmount());
        policy.setStartDate(req.startDate());

        when(policyMapper.toEntity(anyString(), eq(req), eq(client), eq(building), eq(broker), eq(currency))).thenReturn(policy);

        when(calculationService.calculateFinalPremium(eq(req.basePremiumAmount()), eq(req.startDate()), eq(fees), anyList()))
                .thenReturn(FINAL_PREMIUM_130);

        Policy saved = new Policy();
        PolicyDetailsResponse dto = mock(PolicyDetailsResponse.class);
        when(policyRepository.save(policy)).thenReturn(saved);
        when(policyMapper.toDetails(saved)).thenReturn(dto);

        PolicyDetailsResponse result = policyService.createDraft(req);

        assertNotNull(result);
        assertSame(dto, result);

        verify(feeRepository).findAllByIdIn(feeIds);
        verify(feeRepository, never()).findActiveForDate(any());
        assertEquals(PolicyStatus.DRAFT, policy.getStatus());
        assertEquals(FINAL_PREMIUM_130, policy.getFinalPremiumAmount());
        verify(policyRepository).save(policy);
        verify(policyMapper).toDetails(saved);
    }

    @Test
    void createDraftShouldUseActiveFeesForDateWhenFeeIdsMissingOrEmpty() {
        PolicyCreateDraftRequest req = draftRequest(List.of());

        Client client = mock(Client.class);
        Building building = mock(Building.class);
        Broker broker = mock(Broker.class);
        Currency currency = mock(Currency.class);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(buildingRepository.findByIdAndOwnerId(BUILDING_ID, CLIENT_ID)).thenReturn(Optional.of(building));
        when(brokerRepository.findById(BROKER_ID)).thenReturn(Optional.of(broker));
        when(broker.getStatus()).thenReturn(BrokerStatus.ACTIVE);
        when(currencyRepository.findById(CURRENCY_ID)).thenReturn(Optional.of(currency));

        List<FeeConfiguration> fees = List.of(mock(FeeConfiguration.class));
        when(feeRepository.findActiveForDate(req.startDate())).thenReturn(fees);

        when(building.getCity()).thenReturn(null);
        when(building.getBuildingType()).thenReturn(null);

        Policy policy = new Policy();
        policy.setBasePremiumAmount(req.basePremiumAmount());
        policy.setStartDate(req.startDate());

        when(policyMapper.toEntity(anyString(), eq(req), eq(client), eq(building), eq(broker), eq(currency))).thenReturn(policy);
        when(calculationService.calculateFinalPremium(eq(req.basePremiumAmount()), eq(req.startDate()), eq(fees), anyList()))
                .thenReturn(FINAL_PREMIUM_110);

        Policy saved = new Policy();
        PolicyDetailsResponse dto = mock(PolicyDetailsResponse.class);
        when(policyRepository.save(policy)).thenReturn(saved);
        when(policyMapper.toDetails(saved)).thenReturn(dto);

        PolicyDetailsResponse result = policyService.createDraft(req);

        assertSame(dto, result);
        verify(feeRepository).findActiveForDate(req.startDate());
        verify(feeRepository, never()).findAllByIdIn(any());
    }

    @Test
    void getByIdShouldThrowNotFoundWhenMissing() {
        when(policyRepository.findById(POLICY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> policyService.getById(POLICY_ID));

        verify(policyRepository).findById(POLICY_ID);
        verifyNoInteractions(policyMapper);
    }

    @Test
    void getByIdShouldReturnDetailsWhenExists() {
        Policy policy = mock(Policy.class);
        PolicyDetailsResponse dto = mock(PolicyDetailsResponse.class);

        when(policyRepository.findById(POLICY_ID)).thenReturn(Optional.of(policy));
        when(policyMapper.toDetails(policy)).thenReturn(dto);

        PolicyDetailsResponse result = policyService.getById(POLICY_ID);

        assertSame(dto, result);
        verify(policyMapper).toDetails(policy);
    }

    @Test
    void searchShouldDelegateToRepositoryAndMapToSummary() {
        Pageable pageable = defaultPageable();

        Policy p1 = mock(Policy.class);
        Policy p2 = mock(Policy.class);

        Page<Policy> page = new PageImpl<>(List.of(p1, p2), pageable, TOTAL_ELEMENTS);

        PolicySummaryResponse s1 = mock(PolicySummaryResponse.class);
        PolicySummaryResponse s2 = mock(PolicySummaryResponse.class);

        when(policyRepository.search(null, null, null, null, null, pageable)).thenReturn(page);
        when(policyMapper.toSummary(p1)).thenReturn(s1);
        when(policyMapper.toSummary(p2)).thenReturn(s2);

        Page<PolicySummaryResponse> result = policyService.search(null, null, null, null, null, pageable);

        assertEquals(TOTAL_ELEMENTS, result.getTotalElements());
        assertSame(s1, result.getContent().get(0));
        assertSame(s2, result.getContent().get(1));
    }

    @Test
    void activateShouldThrowNotFoundWhenPolicyMissing() {
        when(policyRepository.findById(POLICY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> policyService.activate(POLICY_ID));
    }

    @Test
    void activateShouldThrowConflictWhenStatusNotDraft() {
        Policy policy = mock(Policy.class);
        when(policyRepository.findById(POLICY_ID)).thenReturn(Optional.of(policy));
        when(policy.getStatus()).thenReturn(PolicyStatus.ACTIVE);

        assertThrows(ConflictException.class, () -> policyService.activate(POLICY_ID));

        verify(policy).getStatus();
        verify(policyRepository, never()).save(any());
    }

    @Test
    void activateShouldThrowConflictWhenStartDateInPast() {
        Policy policy = mock(Policy.class);
        when(policyRepository.findById(POLICY_ID)).thenReturn(Optional.of(policy));
        when(policy.getStatus()).thenReturn(PolicyStatus.DRAFT);
        when(policy.getStartDate()).thenReturn(LocalDate.now().minusDays(1));

        assertThrows(ConflictException.class, () -> policyService.activate(POLICY_ID));

        verify(policyRepository, never()).save(any());
    }

    @Test
    void activateShouldSetStatusActiveAndSaveAndReturnDetails() {
        Policy policy = new Policy();
        policy.setStatus(PolicyStatus.DRAFT);
        policy.setStartDate(LocalDate.now().plusDays(1));

        when(policyRepository.findById(POLICY_ID)).thenReturn(Optional.of(policy));

        Policy saved = mock(Policy.class);
        PolicyDetailsResponse dto = mock(PolicyDetailsResponse.class);

        when(policyRepository.save(policy)).thenReturn(saved);
        when(policyMapper.toDetails(saved)).thenReturn(dto);

        PolicyDetailsResponse result = policyService.activate(POLICY_ID);

        assertSame(dto, result);
        assertEquals(PolicyStatus.ACTIVE, policy.getStatus());
        assertNotNull(policy.getActivatedAt());
        verify(policyRepository).save(policy);
    }

    @Test
    void cancelShouldThrowConflictWhenStatusNotActive() {
        Policy policy = mock(Policy.class);
        when(policyRepository.findById(POLICY_ID)).thenReturn(Optional.of(policy));
        when(policy.getStatus()).thenReturn(PolicyStatus.DRAFT);

        PolicyCancelRequest request = new PolicyCancelRequest(CANCEL_REASON_SHORT);

        assertThrows(ConflictException.class, () -> policyService.cancel(POLICY_ID, request));

        verify(policyRepository, never()).save(any());
    }

    @Test
    void cancelShouldSetCancelledFieldsAndSaveAndReturnDetails() {
        Policy policy = new Policy();
        policy.setStatus(PolicyStatus.ACTIVE);

        when(policyRepository.findById(POLICY_ID)).thenReturn(Optional.of(policy));

        Policy saved = mock(Policy.class);
        PolicyDetailsResponse dto = mock(PolicyDetailsResponse.class);

        when(policyRepository.save(policy)).thenReturn(saved);
        when(policyMapper.toDetails(saved)).thenReturn(dto);

        PolicyDetailsResponse result = policyService.cancel(
                POLICY_ID,
                new PolicyCancelRequest(CANCEL_REASON_CUSTOMER_REQUEST)
        );

        assertSame(dto, result);
        assertEquals(PolicyStatus.CANCELLED, policy.getStatus());
        assertNotNull(policy.getCancelledAt());
        assertEquals(CANCEL_REASON_CUSTOMER_REQUEST, policy.getCancellationReason());
        verify(policyRepository).save(policy);
    }

    @Test
    void createDraftShouldAddCityRisksWhenCountyIsNull() {
        PolicyCreateDraftRequest req = draftRequest(null);

        Client client = mock(Client.class);
        Building building = mock(Building.class, RETURNS_DEEP_STUBS);
        Broker broker = mock(Broker.class);
        Currency currency = mock(Currency.class);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(buildingRepository.findByIdAndOwnerId(BUILDING_ID, CLIENT_ID)).thenReturn(Optional.of(building));
        when(brokerRepository.findById(BROKER_ID)).thenReturn(Optional.of(broker));
        when(broker.getStatus()).thenReturn(BrokerStatus.ACTIVE);
        when(currencyRepository.findById(CURRENCY_ID)).thenReturn(Optional.of(currency));

        List<FeeConfiguration> fees = List.of(mock(FeeConfiguration.class));
        when(feeRepository.findActiveForDate(req.startDate())).thenReturn(fees);

        when(building.getCity().getId()).thenReturn(111L);
        when(building.getCity().getCounty()).thenReturn(null);
        when(building.getBuildingType()).thenReturn(null);

        when(riskRepository.findByLevelAndReferenceIdAndActiveTrue(any(), anyLong()))
                .thenReturn(List.of());

        Policy policy = new Policy();
        when(policyMapper.toEntity(anyString(), eq(req), eq(client), eq(building), eq(broker), eq(currency)))
                .thenReturn(policy);

        when(calculationService.calculateFinalPremium(any(), any(), anyList(), anyList()))
                .thenReturn(FINAL_PREMIUM_130);

        Policy saved = new Policy();
        when(policyRepository.save(policy)).thenReturn(saved);
        when(policyMapper.toDetails(saved)).thenReturn(mock(PolicyDetailsResponse.class));

        policyService.createDraft(req);

        verify(riskRepository).findByLevelAndReferenceIdAndActiveTrue(RiskLevel.CITY, 111L);
        verify(riskRepository, never()).findByLevelAndReferenceIdAndActiveTrue(eq(RiskLevel.COUNTY), anyLong());
        verify(riskRepository, never()).findByLevelAndReferenceIdAndActiveTrue(eq(RiskLevel.COUNTRY), anyLong());
    }

    @Test
    void createDraftShouldAddCityAndCountyRisksWhenCountryIsNull() {
        PolicyCreateDraftRequest req = draftRequest(null);

        Client client = mock(Client.class);
        Building building = mock(Building.class, RETURNS_DEEP_STUBS);
        Broker broker = mock(Broker.class);
        Currency currency = mock(Currency.class);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(buildingRepository.findByIdAndOwnerId(BUILDING_ID, CLIENT_ID)).thenReturn(Optional.of(building));
        when(brokerRepository.findById(BROKER_ID)).thenReturn(Optional.of(broker));
        when(broker.getStatus()).thenReturn(BrokerStatus.ACTIVE);
        when(currencyRepository.findById(CURRENCY_ID)).thenReturn(Optional.of(currency));

        List<FeeConfiguration> fees = List.of(mock(FeeConfiguration.class));
        when(feeRepository.findActiveForDate(req.startDate())).thenReturn(fees);

        when(building.getCity().getId()).thenReturn(111L);
        when(building.getCity().getCounty().getId()).thenReturn(222L);
        when(building.getCity().getCounty().getCountry()).thenReturn(null);
        when(building.getBuildingType()).thenReturn(null);

        when(riskRepository.findByLevelAndReferenceIdAndActiveTrue(any(), anyLong()))
                .thenReturn(List.of());

        Policy policy = new Policy();
        when(policyMapper.toEntity(anyString(), eq(req), eq(client), eq(building), eq(broker), eq(currency)))
                .thenReturn(policy);

        when(calculationService.calculateFinalPremium(any(), any(), anyList(), anyList()))
                .thenReturn(FINAL_PREMIUM_130);

        Policy saved = new Policy();
        when(policyRepository.save(policy)).thenReturn(saved);
        when(policyMapper.toDetails(saved)).thenReturn(mock(PolicyDetailsResponse.class));

        policyService.createDraft(req);

        verify(riskRepository).findByLevelAndReferenceIdAndActiveTrue(RiskLevel.CITY, 111L);
        verify(riskRepository).findByLevelAndReferenceIdAndActiveTrue(RiskLevel.COUNTY, 222L);
        verify(riskRepository, never()).findByLevelAndReferenceIdAndActiveTrue(eq(RiskLevel.COUNTRY), anyLong());
    }

}
