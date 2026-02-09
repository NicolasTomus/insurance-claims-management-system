package com.insurance.backend.service;

import com.insurance.backend.domain.broker.Broker;
import com.insurance.backend.domain.broker.BrokerStatus;
import com.insurance.backend.domain.building.Building;
import com.insurance.backend.domain.client.Client;
import com.insurance.backend.domain.metadata.currency.Currency;
import com.insurance.backend.domain.metadata.fee.FeeConfiguration;
import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final RiskFactorConfigurationRepository riskRepository;
    private final PolicyMapper policyMapper;
    private final PolicyCalculationService calculationService;

    public PolicyService(PolicyRepository policyRepository,
                         ClientRepository clientRepository,
                         BuildingRepository buildingRepository,
                         BrokerRepository brokerRepository,
                         CurrencyRepository currencyRepository,
                         FeeConfigurationRepository feeRepository,
                         RiskFactorConfigurationRepository riskRepository,
                         PolicyMapper policyMapper,
                         PolicyCalculationService calculationService) {
        this.policyRepository = policyRepository;
        this.clientRepository = clientRepository;
        this.buildingRepository = buildingRepository;
        this.brokerRepository = brokerRepository;
        this.currencyRepository = currencyRepository;
        this.feeRepository = feeRepository;
        this.riskRepository = riskRepository;
        this.policyMapper = policyMapper;
        this.calculationService = calculationService;
    }

    @Transactional
    public PolicyDetailsResponse createDraft(PolicyCreateDraftRequest request) {

        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new NotFoundException("Client not found: " + request.clientId()));

        Building building = buildingRepository.findByIdAndOwnerId(request.buildingId(), request.clientId())
                .orElseThrow(() -> new ConflictException(
                        "Building " + request.buildingId() + " is not owned by client " + request.clientId()
                ));

        Broker broker = brokerRepository.findById(request.brokerId())
                .orElseThrow(() -> new NotFoundException("Broker not found: " + request.brokerId()));

        if (broker.getStatus() != BrokerStatus.ACTIVE) {
            throw new ConflictException("Broker is inactive: " + broker.getId());
        }

        Currency currency = currencyRepository.findById(request.currencyId())
                .orElseThrow(() -> new NotFoundException("Currency not found: " + request.currencyId()));

        List<FeeConfiguration> fees;
        if (request.feeConfigurationIds() != null && !request.feeConfigurationIds().isEmpty()) {
            fees = feeRepository.findAllByIdIn(request.feeConfigurationIds());
        } else {
            fees = feeRepository.findActiveForDate(request.startDate());
        }

        List<RiskFactorConfiguration> risks = new ArrayList<>();

        if (building.getCity() != null) {
            if (building.getCity().getCounty() != null) {

                if (building.getCity().getCounty().getCountry() != null) {
                    risks.addAll(riskRepository.findByLevelAndReferenceIdAndActiveTrue(
                                    RiskLevel.COUNTRY, building.getCity().getCounty().getCountry().getId()
                            )
                    );
                }

                risks.addAll(riskRepository.findByLevelAndReferenceIdAndActiveTrue(
                                RiskLevel.COUNTY, building.getCity().getCounty().getId()
                        )
                );
            }

            risks.addAll(riskRepository.findByLevelAndReferenceIdAndActiveTrue(
                            RiskLevel.CITY, building.getCity().getId()
                    )
            );
        }

        if (building.getBuildingType() != null) {
            long typeRef = building.getBuildingType().ordinal();
            risks.addAll(riskRepository.findByLevelAndReferenceIdAndActiveTrue(
                            RiskLevel.BUILDING_TYPE, typeRef
                    )
            );
        }

        String policyNumber = generatePolicyNumber();

        Policy policy = policyMapper.toEntity(policyNumber, request, client, building, broker, currency);
        policy.setStatus(PolicyStatus.DRAFT);

        policy.setFinalPremiumAmount(
                calculationService.calculateFinalPremium(policy.getBasePremiumAmount(),
                        policy.getStartDate(), fees, risks
                )
        );

        Policy saved = policyRepository.save(policy);
        log.info("Policy draft created: id={}, policyNumber={}", saved.getId(), saved.getPolicyNumber());
        return policyMapper.toDetails(saved);
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
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new NotFoundException("Policy not found:  " + policyId));

        if (policy.getStatus() != PolicyStatus.DRAFT) {
            throw new ConflictException("Only DRAFT policies can be activated");
        }

        if (policy.getStartDate() != null && policy.getStartDate().isBefore(LocalDate.now())) {
            throw new ConflictException("Policy start date is in the past, cannot activate");
        }

        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setActivatedAt(LocalDateTime.now());

        Policy saved = policyRepository.save(policy);
        log.info("Policy activated: id={}", saved.getId());
        return policyMapper.toDetails(saved);
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
