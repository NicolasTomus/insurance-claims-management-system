package com.insurance.backend.service.risk;

import com.insurance.backend.domain.building.Building;
import com.insurance.backend.domain.building.BuildingType;
import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;
import com.insurance.backend.domain.metadata.risk.RiskLevel;
import com.insurance.backend.infrastructure.persistence.repository.metadata.risk.RiskFactorConfigurationRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(4)
public class BuildingTypeRiskFactorStrategy implements RiskFactorStrategy {

    private final RiskFactorConfigurationRepository riskRepository;

    public BuildingTypeRiskFactorStrategy(RiskFactorConfigurationRepository riskRepository) {
        this.riskRepository = riskRepository;
    }

    @Override
    public List<RiskFactorConfiguration> resolve(Building building) {
        if (building == null) {
            return List.of();
        }
        BuildingType type = building.getBuildingType();
        if (type == null) {
            return List.of();
        }

        long typeRef = type.ordinal();
        return riskRepository.findByLevelAndReferenceIdAndActiveTrue(
                RiskLevel.BUILDING_TYPE,
                typeRef
        );
    }
}
