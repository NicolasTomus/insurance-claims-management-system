package com.insurance.backend.service.risk;

import com.insurance.backend.domain.building.Building;
import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;
import com.insurance.backend.domain.metadata.risk.RiskLevel;
import com.insurance.backend.infrastructure.persistence.repository.metadata.risk.RiskFactorConfigurationRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
public class CityRiskFactorStrategy implements RiskFactorStrategy {

    private final RiskFactorConfigurationRepository riskRepository;

    public CityRiskFactorStrategy(RiskFactorConfigurationRepository riskRepository) {
        this.riskRepository = riskRepository;
    }

    @Override
    public List<RiskFactorConfiguration> resolve(Building building) {
        if (building == null || building.getCity() == null) {
            return List.of();
        }

        return riskRepository.findByLevelAndReferenceIdAndActiveTrue(
                RiskLevel.CITY,
                building.getCity().getId()
        );
    }
}
