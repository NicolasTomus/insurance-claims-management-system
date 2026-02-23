package com.insurance.backend.service.risk;

import com.insurance.backend.domain.building.Building;
import com.insurance.backend.domain.geography.City;
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
class CityRiskFactorStrategyTest {

    @Mock
    private RiskFactorConfigurationRepository riskRepository;

    @InjectMocks
    private CityRiskFactorStrategy strategy;

    @Test
    void resolveShouldReturnEmptyList_whenBuildingIsNull() {
        List<RiskFactorConfiguration> result = strategy.resolve(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(riskRepository);
    }

    @Test
    void resolveShouldReturnEmptyList_whenCityIsNull() {
        Building building = mock(Building.class);
        when(building.getCity()).thenReturn(null);

        List<RiskFactorConfiguration> result = strategy.resolve(building);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(riskRepository);
    }

    @Test
    void resolveShouldQueryRepository_whenCityExists() {
        Building building = mock(Building.class);
        City city = mock(City.class);
        when(building.getCity()).thenReturn(city);
        when(city.getId()).thenReturn(7L);

        RiskFactorConfiguration cfg = mock(RiskFactorConfiguration.class);
        when(riskRepository.findByLevelAndReferenceIdAndActiveTrue(RiskLevel.CITY, 7L))
                .thenReturn(List.of(cfg));

        List<RiskFactorConfiguration> result = strategy.resolve(building);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(cfg, result.get(0));

        verify(riskRepository, times(1))
                .findByLevelAndReferenceIdAndActiveTrue(RiskLevel.CITY, 7L);
        verifyNoMoreInteractions(riskRepository);
    }
}