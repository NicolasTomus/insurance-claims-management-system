package com.insurance.backend.service.risk;

import com.insurance.backend.domain.building.Building;
import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;

import java.util.List;

public interface RiskFactorStrategy {
    List<RiskFactorConfiguration> resolve(Building building);
}
