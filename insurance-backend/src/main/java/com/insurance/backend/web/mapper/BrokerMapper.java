package com.insurance.backend.web.mapper;

import com.insurance.backend.domain.broker.Broker;
import com.insurance.backend.domain.broker.BrokerStatus;
import com.insurance.backend.web.dto.broker.BrokerCreateRequest;
import com.insurance.backend.web.dto.broker.BrokerDetailsResponse;
import com.insurance.backend.web.dto.broker.BrokerSummaryResponse;
import com.insurance.backend.web.dto.broker.BrokerUpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class BrokerMapper {

    public Broker toEntity(BrokerCreateRequest request) {
        return new Broker(
                request.brokerCode(),
                request.name(),
                request.email(),
                request.phone(),
                BrokerStatus.ACTIVE,
                request.commissionPercentage()
        );
    }

    public void applyUpdate(Broker broker, BrokerUpdateRequest request) {
        if (request.name() != null) {
            broker.setName(request.name());
        }
        if (request.email() != null) {
            broker.setEmail(request.email());
        }
        if (request.phone() != null) {
            broker.setPhone(request.phone());
        }
        if (request.status() != null) {
            broker.setStatus(request.status());
        }
        if (request.commissionPercentage() != null) {
            broker.setCommissionPercentage(request.commissionPercentage());
        }
    }

    public BrokerDetailsResponse toDetails(Broker broker) {
        return new BrokerDetailsResponse(
                broker.getId(),
                broker.getBrokerCode(),
                broker.getName(),
                broker.getEmail(),
                broker.getPhone(),
                broker.getStatus(),
                broker.getCommissionPercentage()
        );
    }

    public BrokerSummaryResponse toSummary(Broker broker) {
        return new BrokerSummaryResponse(
                broker.getId(),
                broker.getBrokerCode(),
                broker.getName(),
                broker.getStatus()
        );
    }
}
