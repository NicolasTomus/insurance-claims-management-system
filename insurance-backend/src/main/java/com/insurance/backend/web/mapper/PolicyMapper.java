package com.insurance.backend.web.mapper;

import com.insurance.backend.domain.broker.Broker;
import com.insurance.backend.domain.building.Building;
import com.insurance.backend.domain.client.Client;
import com.insurance.backend.domain.metadata.currency.Currency;
import com.insurance.backend.domain.policy.Policy;
import com.insurance.backend.web.dto.broker.BrokerSummaryResponse;
import com.insurance.backend.web.dto.building.BuildingDetailsResponse;
import com.insurance.backend.web.dto.client.ClientSummaryResponse;
import com.insurance.backend.web.dto.policy.PolicyCreateDraftRequest;
import com.insurance.backend.web.dto.policy.PolicyDetailsResponse;
import com.insurance.backend.web.dto.policy.PolicySummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class PolicyMapper {

    private final ClientMapper clientMapper;
    private final BuildingMapper buildingMapper;
    private final BrokerMapper brokerMapper;

    public PolicyMapper(ClientMapper clientMapper, BuildingMapper buildingMapper, BrokerMapper brokerMapper) {
        this.clientMapper = clientMapper;
        this.buildingMapper = buildingMapper;
        this.brokerMapper = brokerMapper;
    }

    public Policy toEntity(String policyNumber,
                           PolicyCreateDraftRequest request,
                           Client client,
                           Building building,
                           Broker broker,
                           Currency currency) {

        Policy policy = new Policy();
        policy.setPolicyNumber(policyNumber);
        policy.setClient(client);
        policy.setBuilding(building);
        policy.setBroker(broker);
        policy.setStartDate(request.startDate());
        policy.setEndDate(request.endDate());
        policy.setBasePremiumAmount(request.basePremiumAmount());
        policy.setCurrency(currency);

        return policy;
    }

    public PolicySummaryResponse toSummary(Policy policy) {
        return new PolicySummaryResponse(
                policy.getId(),
                policy.getPolicyNumber(),
                policy.getStatus(),
                policy.getStartDate(),
                policy.getEndDate(),
                policy.getFinalPremiumAmount(),
                policy.getCurrency() != null ? policy.getCurrency().getCode() : null
        );
    }

    public PolicyDetailsResponse toDetails(Policy policy) {
        ClientSummaryResponse client = clientMapper.toSummary(policy.getClient());
        BuildingDetailsResponse building = buildingMapper.toDetails(policy.getBuilding());
        BrokerSummaryResponse broker = brokerMapper.toSummary(policy.getBroker());

        return new PolicyDetailsResponse(
                policy.getId(),
                policy.getPolicyNumber(),
                policy.getStatus(),
                policy.getStartDate(),
                policy.getEndDate(),
                policy.getBasePremiumAmount(),
                policy.getFinalPremiumAmount(),
                policy.getCurrency() != null ? policy.getCurrency().getCode() : null,
                client,
                building,
                broker
        );
    }
}
