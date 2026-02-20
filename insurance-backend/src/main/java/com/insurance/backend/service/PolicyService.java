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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PolicyService {

    private static final Logger log = LoggerFactory.getLogger(PolicyService.class);

    private final PolicyRepository policyRepository;
    private final ClientRepository clientRepository;
    private final BuildingRepository buildingRepository;
    private final BrokerRepository brokerRepository;
    private final CurrencyRepository currencyRepository;
    private final FeeConfigurationRepository feeRepository;
    private final PolicyMapper policyMapper;
    private final PolicyCalculationService calculationService;

    private final List<RiskFactorStrategy> riskStrategies;

    public PolicyService(PolicyRepository policyRepository,
                         ClientRepository clientRepository,
                         BuildingRepository buildingRepository,
                         BrokerRepository brokerRepository,
                         CurrencyRepository currencyRepository,
                         FeeConfigurationRepository feeRepository,
                         PolicyMapper policyMapper,
                         PolicyCalculationService calculationService,
                         List<RiskFactorStrategy> riskStrategies) {

        this.policyRepository = policyRepository;
        this.clientRepository = clientRepository;
        this.buildingRepository = buildingRepository;
        this.brokerRepository = brokerRepository;
        this.currencyRepository = currencyRepository;
        this.feeRepository = feeRepository;
        this.policyMapper = policyMapper;
        this.calculationService = calculationService;
        this.riskStrategies = riskStrategies;
    }

    @Scheduled(cron = "0 1 0 * * *")
    @Transactional
    public void markExpiredPolicies() {
        int updated = policyRepository.bulkExpire(LocalDate.now(), Instant.now());
        log.info("Expired {} policies", updated);
    }

    @Transactional
    public PolicyDetailsResponse createDraft(PolicyCreateDraftRequest request) {

        Client client = getClient(request.clientId());
        Building building = getBuildingOwnedByClient(request.buildingId(), request.clientId());
        Broker broker = getActiveBroker(request.brokerId());
        Currency currency = getCurrency(request.currencyId());

        List<FeeConfiguration> fees = getFeesForDraft(request);
        List<RiskFactorConfiguration> risks = getRisksForBuilding(building);

        Policy policy = createDraftPolicy(request, client, building, broker, currency, fees, risks);

        Policy saved = policyRepository.save(policy);
        log.info("Policy draft created: id={}, policyNumber={}", saved.getId(), saved.getPolicyNumber());
        return policyMapper.toDetails(saved);
    }

    private Policy createDraftPolicy(PolicyCreateDraftRequest request,
                                     Client client,
                                     Building building,
                                     Broker broker,
                                     Currency currency,
                                     List<FeeConfiguration> fees,
                                     List<RiskFactorConfiguration> risks) {

        String policyNumber = generatePolicyNumber();

        Policy policy = policyMapper.toEntity(policyNumber, request, client, building, broker, currency);
        policy.setStatus(PolicyStatus.DRAFT);

        policy.setFinalPremiumAmount(
                calculationService.calculateFinalPremium(
                        policy.getBasePremiumAmount(),
                        policy.getStartDate(),
                        fees,
                        risks
                )
        );

        return policy;
    }

    private Client getClient(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found: " + clientId));
    }

    private Building getBuildingOwnedByClient(Long buildingId, Long clientId) {
        return buildingRepository.findByIdAndOwnerId(buildingId, clientId)
                .orElseThrow(() -> new ConflictException(
                        "Building " + buildingId + " is not owned by client " + clientId
                ));
    }

    private Broker getActiveBroker(Long brokerId) {
        Broker broker = brokerRepository.findById(brokerId)
                .orElseThrow(() -> new NotFoundException("Broker not found: " + brokerId));

        if (broker.getStatus() != BrokerStatus.ACTIVE) {
            throw new ConflictException("Broker is inactive: " + broker.getId());
        }

        return broker;
    }

    private Currency getCurrency(Long currencyId) {
        return currencyRepository.findById(currencyId)
                .orElseThrow(() -> new NotFoundException("Currency not found: " + currencyId));
    }

    private List<FeeConfiguration> getFeesForDraft(PolicyCreateDraftRequest request) {
        if (request.feeConfigurationIds() != null && !request.feeConfigurationIds().isEmpty()) {
            return feeRepository.findAllByIdIn(request.feeConfigurationIds());
        }
        return feeRepository.findActiveForDate(request.startDate());
    }

    private List<RiskFactorConfiguration> getRisksForBuilding(Building building) {
        return riskStrategies.stream()
                .flatMap(s -> s.resolve(building).stream())
                .toList();
    }

    @Transactional(readOnly = true)
    public PolicyDetailsResponse getById(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new NotFoundException("Policy not found: " + policyId));
        return policyMapper.toDetails(policy);
    }

    @Transactional(readOnly = true)
    public Page<PolicySummaryResponse> search(Long clientId, Long brokerId, PolicyStatus status,
                                              LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return policyRepository
                .search(clientId, brokerId, status, startDate, endDate, pageable)
                .map(policyMapper::toSummary);
    }

    @Transactional
    public PolicyDetailsResponse activate(Long policyId) {
        Policy policy = getPolicy(policyId);

        ensureDraft(policy);
        ensureStartDateNotInPast(policy);

        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setActivatedAt(LocalDateTime.now());

        Policy saved = policyRepository.save(policy);
        log.info("Policy activated: id={}", saved.getId());
        return policyMapper.toDetails(saved);
    }

    private Policy getPolicy(Long policyId) {
        return policyRepository.findById(policyId)
                .orElseThrow(() -> new NotFoundException("Policy not found:  " + policyId));
    }

    private void ensureDraft(Policy policy) {
        if (policy.getStatus() != PolicyStatus.DRAFT) {
            throw new ConflictException("Only DRAFT policies can be activated");
        }
    }

    private void ensureStartDateNotInPast(Policy policy) {
        if (policy.getStartDate() != null && policy.getStartDate().isBefore(LocalDate.now())) {
            throw new ConflictException("Policy start date is in the past, cannot activate");
        }
    }

    @Transactional
    public PolicyDetailsResponse cancel(Long policyId, PolicyCancelRequest request) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new NotFoundException("Policy not found: " + policyId));

        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new ConflictException("Only ACTIVE policies can be cancelled");
        }

        policy.setStatus(PolicyStatus.CANCELLED);
        policy.setCancelledAt(LocalDateTime.now());
        policy.setCancellationReason(request.reason());

        Policy saved = policyRepository.save(policy);
        log.info("Policy cancelled: id={}, reason={}", saved.getId(), request.reason());
        return policyMapper.toDetails(saved);
    }

    private String generatePolicyNumber() {
        return "POL-" + System.currentTimeMillis();
    }
}
