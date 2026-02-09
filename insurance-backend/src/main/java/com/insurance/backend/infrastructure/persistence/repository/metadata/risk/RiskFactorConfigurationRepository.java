package com.insurance.backend.infrastructure.persistence.repository.metadata.risk;

import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;
import com.insurance.backend.domain.metadata.risk.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskFactorConfigurationRepository extends JpaRepository<RiskFactorConfiguration, Long> {

    List<RiskFactorConfiguration> findByLevelAndReferenceIdAndActiveTrue(RiskLevel level, Long referenceId);
}
