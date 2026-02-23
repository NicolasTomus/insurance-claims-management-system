package com.insurance.backend.service.risk;

import com.insurance.backend.domain.building.Building;
import com.insurance.backend.domain.building.BuildingType;
import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;
import com.insurance.backend.domain.metadata.risk.RiskLevel;
import com.insurance.backend.infrastructure.persistence.repository.metadata.risk.RiskFactorConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildingTypeRiskFactorStrategyTest {

    @Mock
    private RiskFactorConfigurationRepository riskRepository;

    @InjectMocks
    private BuildingTypeRiskFactorStrategy strategy;

    @Test
    void resolveShouldReturnEmptyList_whenBuildingIsNull() {
        List<RiskFactorConfiguration> result = strategy.resolve(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(riskRepository);
    }

    @Test
    void resolveShouldReturnEmptyList_whenBuildingTypeIsNull() {
        Building building = mock(Building.class);
        when(building.getBuildingType()).thenReturn(null);

        List<RiskFactorConfiguration> result = strategy.resolve(building);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(riskRepository);
    }

    @Test
    void resolveShouldQueryRepository_whenBuildingTypeExists() {
        Building building = mock(Building.class);

        // Nu depindem de un enum constant anume; luăm primul disponibil.
        BuildingType type = BuildingType.values()[0];
        when(building.getBuildingType()).thenReturn(type);

        long expectedRef = type.ordinal();

        RiskFactorConfiguration cfg = mock(RiskFactorConfiguration.class);
        when(riskRepository.findByLevelAndReferenceIdAndActiveTrue(RiskLevel.BUILDING_TYPE, expectedRef))
                .thenReturn(List.of(cfg));

        List<RiskFactorConfiguration> result = strategy.resolve(building);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(cfg, result.get(0));

        verify(riskRepository, times(1))
                .findByLevelAndReferenceIdAndActiveTrue(RiskLevel.BUILDING_TYPE, expectedRef);
        verifyNoMoreInteractions(riskRepository);
    }
}