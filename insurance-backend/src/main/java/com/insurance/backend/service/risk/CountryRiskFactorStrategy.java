package com.insurance.backend.service.risk;

import com.insurance.backend.domain.building.Building;
import com.insurance.backend.domain.geography.City;
import com.insurance.backend.domain.geography.County;
import com.insurance.backend.domain.geography.Country;
import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;
import com.insurance.backend.domain.metadata.risk.RiskLevel;
import com.insurance.backend.infrastructure.persistence.repository.metadata.risk.RiskFactorConfigurationRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(3)
public class CountryRiskFactorStrategy implements RiskFactorStrategy {

    private final RiskFactorConfigurationRepository riskRepository;

    public CountryRiskFactorStrategy(RiskFactorConfigurationRepository riskRepository) {
        this.riskRepository = riskRepository;
    }

    @Override
    public List<RiskFactorConfiguration> resolve(Building building) {
        if (building == null) {
            return List.of();
        }
        City city = building.getCity();
        if (city == null) {
            return List.of();
        }
        County county = city.getCounty();
        if (county == null) {
            return List.of();
        }
        Country country = county.getCountry();
        if (country == null) {
            return List.of();
        }

        return riskRepository.findByLevelAndReferenceIdAndActiveTrue(
                RiskLevel.COUNTRY,
                country.getId()
        );
    }
}
