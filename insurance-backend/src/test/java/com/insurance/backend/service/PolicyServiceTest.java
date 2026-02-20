package com.insurance.backend.service;

import com.insurance.backend.domain.broker.Broker;
import com.insurance.backend.domain.broker.BrokerStatus;
import com.insurance.backend.domain.building.Building;
import com.insurance.backend.domain.client.Client;
import com.insurance.backend.domain.metadata.currency.Currency;
import com.insurance.backend.domain.metadata.fee.FeeConfiguration;
import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;
import com.insurance.backend.domain.policy.Policy;
import com.insurance.backend.domain.policy.PolicyStatus;
import com.insurance.backend.infrastructure.persistence.repository.broker.BrokerRepository;
import com.insurance.backend.infrastructure.persistence.repository.building.BuildingRepository;
import com.insurance.backend.infrastructure.persistence.repository.client.ClientRepository;
import com.insurance.backend.infrastructure.persistence.repository.metadata.currency.CurrencyRepository;
import com.insurance.backend.infrastructure.persistence.repository.metadata.fee.FeeConfigurationRepository;
import com.insurance.backend.infrastructure.persistence.repository.policy.PolicyRepository;
import com.insurance.backend.service.risk.RiskFactorStrategy;
import com.insurance.backend.web.dto.policy.PolicyCancelRequest;
import com.insurance.backend.web.dto.policy.PolicyCreateDraftRequest;
import com.insurance.backend.web.dto.policy.PolicyDetailsResponse;
import com.insurance.backend.web.dto.policy.PolicySummaryResponse;
import com.insurance.backend.web.exception.ConflictException;
import com.insurance.backend.web.exception.NotFoundException;
import com.insurance.backend.web.mapper.PolicyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    private static final long POLICY_ID = 100L;
    private static final long CLIENT_ID = 10L;
    private static final long BUILDING_ID = 20L;
    private static final long BROKER_ID = 30L;
    private static final long CURRENCY_ID = 40L;

    private static final BigDecimal BASE_PREMIUM = new BigDecimal("100.00");
    private static final BigDecimal FINAL_PREMIUM = new BigDecimal("123.45");

    private static final LocalDate START = LocalDate.now().plusDays(1);
    private static final LocalDate END = START.plusDays(30);

    @Mock private PolicyRepository policyRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private BuildingRepository buildingRepository;
    @Mock private BrokerRepository brokerRepository;
    @Mock private CurrencyRepository currencyRepository;
    @Mock private FeeConfigurationRepository feeRepository;
    @Mock private PolicyMapper policyMapper;
    @Mock private PolicyCalculationService calculationService;

    @Mock private RiskFactorStrategy strategy1;
    @Mock private RiskFactorStrategy strategy2;


    @BeforeEach
    void setUp() {
        policyService = new PolicyService(
                policyRepository,
                clientRepository,
                buildingRepository,
                brokerRepository,
                currencyRepository,
                feeRepository,
                policyMapper,
                calculationService,
                java.util.List.of(strategy1, strategy2)
        );

        org.mockito.Mockito.lenient().when(strategy1.resolve(org.mockito.ArgumentMatchers.any())).thenReturn(java.util.List.of());
        org.mockito.Mockito.lenient().when(strategy2.resolve(org.mockito.ArgumentMatchers.any())).thenReturn(java.util.List.of());
    }

    private PolicyService policyService;

    @Test
    void createDraftShouldThrowNotFoundWhenClientMissing() {
        PolicyCreateDraftRequest req = new PolicyCreateDraftRequest(
                CLIENT_ID, BUILDING_ID, BROKER_ID, START, END, BASE_PREMIUM, CURRENCY_ID, null
        );

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> policyService.createDraft(req));

        verify(clientRepository).findById(CLIENT_ID);
        verifyNoMoreInteractions(clientRepository);
        verifyNoInteractions(buildingRepository, brokerRepository, currencyRepository, feeRepository, policyMapper, calculationService, policyRepository, strategy1, strategy2);
    }

    @Test
    void createDraftShouldThrowConflictWhenBuildingNotOwnedByClient() {
        PolicyCreateDraftRequest req = new PolicyCreateDraftRequest(
                CLIENT_ID, BUILDING_ID, BROKER_ID, START, END, BASE_PREMIUM, CURRENCY_ID, null
        );

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(mock(Client.class)));
        when(buildingRepository.findByIdAndOwnerId(BUILDING_ID, CLIENT_ID)).thenReturn(Optional.empty());

        assertThrows(ConflictException.class, () -> policyService.createDraft(req));

        verify(clientRepository).findById(CLIENT_ID);
        verify(buildingRepository).findByIdAndOwnerId(BUILDING_ID, CLIENT_ID);
        verifyNoInteractions(brokerRepository, currencyRepository, feeRepository, policyMapper, calculationService, policyRepository, strategy1, strategy2);
    }

    @Test
    void createDraftShouldThrowConflictWhenBrokerInactive() {
        PolicyCreateDraftRequest req = new PolicyCreateDraftRequest(
                CLIENT_ID, BUILDING_ID, BROKER_ID, START, END, BASE_PREMIUM, CURRENCY_ID, null
        );

        Client client = mock(Client.class);
        Building building = mock(Building.class);
        Broker broker = mock(Broker.class);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(buildingRepository.findByIdAndOwnerId(BUILDING_ID, CLIENT_ID)).thenReturn(Optional.of(building));
        when(brokerRepository.findById(BROKER_ID)).thenReturn(Optional.of(broker));
        when(broker.getStatus()).thenReturn(BrokerStatus.INACTIVE);
        when(broker.getId()).thenReturn(BROKER_ID);

        assertThrows(ConflictException.class, () -> policyService.createDraft(req));

        verify(clientRepository).findById(CLIENT_ID);
        verify(buildingRepository).findByIdAndOwnerId(BUILDING_ID, CLIENT_ID);
        verify(brokerRepository).findById(BROKER_ID);
        verify(broker).getStatus();
        verify(broker).getId();
        verifyNoInteractions(currencyRepository, feeRepository, policyMapper, calculationService, policyRepository, strategy1, strategy2);
    }

    @Test
    void createDraftShouldUseFeeIdsWhenProvidedAndCalculatePremiumAndSave() {
        List<Long> feeIds = List.of(1L, 2L);
        PolicyCreateDraftRequest req = new PolicyCreateDraftRequest(
                CLIENT_ID, BUILDING_ID, BROKER_ID, START, END, BASE_PREMIUM, CURRENCY_ID, feeIds
        );

        Client client = mock(Client.class);
        Building building = mock(Building.class);
        Broker broker = mock(Broker.class);
        Currency currency = mock(Currency.class);

        FeeConfiguration fee1 = mock(FeeConfiguration.class);
        FeeConfiguration fee2 = mock(FeeConfiguration.class);
        List<FeeConfiguration> fees = List.of(fee1, fee2);

        RiskFactorConfiguration r1 = mock(RiskFactorConfiguration.class);
        RiskFactorConfiguration r2 = mock(RiskFactorConfiguration.class);
        List<RiskFactorConfiguration> risks1 = List.of(r1);
        List<RiskFactorConfiguration> risks2 = List.of(r2);

        Policy mapped = new Policy();
        mapped.setBasePremiumAmount(BASE_PREMIUM);
        mapped.setStartDate(START);

        Policy saved = new Policy();

        PolicyDetailsResponse details = mock(PolicyDetailsResponse.class);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(buildingRepository.findByIdAndOwnerId(BUILDING_ID, CLIENT_ID)).thenReturn(Optional.of(building));
        when(brokerRepository.findById(BROKER_ID)).thenReturn(Optional.of(broker));
        when(broker.getStatus()).thenReturn(BrokerStatus.ACTIVE);
        when(currencyRepository.findById(CURRENCY_ID)).thenReturn(Optional.of(currency));

        when(feeRepository.findAllByIdIn(feeIds)).thenReturn(fees);

        when(strategy1.resolve(building)).thenReturn(risks1);
        when(strategy2.resolve(building)).thenReturn(risks2);

        when(policyMapper.toEntity(anyString(), eq(req), eq(client), eq(building), eq(broker), eq(currency)))
                .thenReturn(mapped);

        when(calculationService.calculateFinalPremium(
                eq(BASE_PREMIUM), eq(START), eq(fees), argThat(list -> list != null && list.size() == 2)
        )).thenReturn(FINAL_PREMIUM);

        when(policyRepository.save(any(Policy.class))).thenReturn(saved);
        when(policyMapper.toDetails(saved)).thenReturn(details);

        PolicyDetailsResponse result = policyService.createDraft(req);

        assertSame(details, result);

        verify(feeRepository).findAllByIdIn(feeIds);
        verify(feeRepository, never()).findActiveForDate(any());

        verify(strategy1).resolve(building);
        verify(strategy2).resolve(building);

        verify(policyMapper).toEntity(anyString(), eq(req), eq(client), eq(building), eq(broker), eq(currency));

        ArgumentCaptor<Policy> toSave = ArgumentCaptor.forClass(Policy.class);
        verify(policyRepository).save(toSave.capture());
        assertEquals(PolicyStatus.DRAFT, toSave.getValue().getStatus());
        assertEquals(FINAL_PREMIUM, toSave.getValue().getFinalPremiumAmount());

        verify(policyMapper).toDetails(saved);
    }

    @Test
    void activateShouldThrowConflictWhenNotDraft() {
        Policy policy = new Policy();
        policy.setStatus(PolicyStatus.ACTIVE);

        when(policyRepository.findById(POLICY_ID)).thenReturn(Optional.of(policy));

        assertThrows(ConflictException.class, () -> policyService.activate(POLICY_ID));

        verify(policyRepository).findById(POLICY_ID);
        verifyNoMoreInteractions(policyRepository);
        verifyNoInteractions(policyMapper);
    }

    @Test
    void activateShouldThrowConflictWhenStartDateInPast() {
        Policy policy = new Policy();
        policy.setStatus(PolicyStatus.DRAFT);
        policy.setStartDate(LocalDate.now().minusDays(1));

        when(policyRepository.findById(POLICY_ID)).thenReturn(Optional.of(policy));

        assertThrows(ConflictException.class, () -> policyService.activate(POLICY_ID));

        verify(policyRepository).findById(POLICY_ID);
        verifyNoMoreInteractions(policyRepository);
        verifyNoInteractions(policyMapper);
    }

    @Test
    void activateShouldSetActiveAndActivatedAtSaveAndReturnDetails() {
        Policy policy = new Policy();
        policy.setStatus(PolicyStatus.DRAFT);
        policy.setStartDate(LocalDate.now().plusDays(2));

        Policy saved = new Policy();
        PolicyDetailsResponse details = mock(PolicyDetailsResponse.class);

        when(policyRepository.findById(POLICY_ID)).thenReturn(Optional.of(policy));
        when(policyRepository.save(policy)).thenReturn(saved);
        when(policyMapper.toDetails(saved)).thenReturn(details);

        PolicyDetailsResponse result = policyService.activate(POLICY_ID);

        assertSame(details, result);
        assertEquals(PolicyStatus.ACTIVE, policy.getStatus());
        assertNotNull(policy.getActivatedAt());

        verify(policyRepository).findById(POLICY_ID);
        verify(policyRepository).save(policy);
        verify(policyMapper).toDetails(saved);
    }

    @Test
    void cancelShouldThrowConflictWhenNotActive() {
        PolicyCancelRequest req = new PolicyCancelRequest("No longer needed");

        Policy policy = new Policy();
        policy.setStatus(PolicyStatus.DRAFT);

        when(policyRepository.findById(POLICY_ID)).thenReturn(Optional.of(policy));

        assertThrows(ConflictException.class, () -> policyService.cancel(POLICY_ID, req));

        verify(policyRepository).findById(POLICY_ID);
        verifyNoMoreInteractions(policyRepository);
        verifyNoInteractions(policyMapper);
    }

    @Test
    void cancelShouldSetCancelledFieldsSaveAndReturnDetails() {
        String reason = "Customer requested";
        PolicyCancelRequest req = new PolicyCancelRequest(reason);

        Policy policy = new Policy();
        policy.setStatus(PolicyStatus.ACTIVE);

        Policy saved = new Policy();
        PolicyDetailsResponse details = mock(PolicyDetailsResponse.class);

        when(policyRepository.findById(POLICY_ID)).thenReturn(Optional.of(policy));
        when(policyRepository.save(policy)).thenReturn(saved);
        when(policyMapper.toDetails(saved)).thenReturn(details);

        PolicyDetailsResponse result = policyService.cancel(POLICY_ID, req);

        assertSame(details, result);
        assertEquals(PolicyStatus.CANCELLED, policy.getStatus());
        assertNotNull(policy.getCancelledAt());
        assertEquals(reason, policy.getCancellationReason());

        verify(policyRepository).findById(POLICY_ID);
        verify(policyRepository).save(policy);
        verify(policyMapper).toDetails(saved);
    }

    @Test
    void getByIdShouldThrowNotFoundWhenMissing() {
        when(policyRepository.findById(POLICY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> policyService.getById(POLICY_ID));

        verify(policyRepository).findById(POLICY_ID);
        verifyNoInteractions(policyMapper);
    }

    @Test
    void searchShouldDelegateToRepositoryAndMapToSummary() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        Policy p1 = mock(Policy.class);
        Policy p2 = mock(Policy.class);
        PolicySummaryResponse s1 = mock(PolicySummaryResponse.class);
        PolicySummaryResponse s2 = mock(PolicySummaryResponse.class);

        Page<Policy> policies = new PageImpl<>(List.of(p1, p2), pageable, 2);

        when(policyRepository.search(CLIENT_ID, BROKER_ID, PolicyStatus.ACTIVE, START, END, pageable)).thenReturn(policies);
        when(policyMapper.toSummary(p1)).thenReturn(s1);
        when(policyMapper.toSummary(p2)).thenReturn(s2);

        Page<PolicySummaryResponse> result = policyService.search(CLIENT_ID, BROKER_ID, PolicyStatus.ACTIVE, START, END, pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(List.of(s1, s2), result.getContent());

        verify(policyRepository).search(CLIENT_ID, BROKER_ID, PolicyStatus.ACTIVE, START, END, pageable);
        verify(policyMapper).toSummary(p1);
        verify(policyMapper).toSummary(p2);
    }

    @Test
    void markExpiredPoliciesShouldCallBulkExpire() {
        when(policyRepository.bulkExpire(any(LocalDate.class), any(Instant.class))).thenReturn(3);

        policyService.markExpiredPolicies();

        verify(policyRepository).bulkExpire(any(LocalDate.class), any(Instant.class));
        verifyNoMoreInteractions(policyRepository);
    }
}
