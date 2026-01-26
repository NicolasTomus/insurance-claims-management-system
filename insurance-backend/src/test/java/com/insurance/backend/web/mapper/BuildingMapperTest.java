package com.insurance.backend.web.mapper;

import com.insurance.backend.domain.building.Building;
import com.insurance.backend.domain.building.BuildingType;
import com.insurance.backend.domain.client.Client;
import com.insurance.backend.domain.geography.City;
import com.insurance.backend.domain.geography.County;
import com.insurance.backend.domain.geography.Country;
import com.insurance.backend.web.dto.building.BuildingCreateRequest;
import com.insurance.backend.web.dto.building.BuildingDetailsResponse;
import com.insurance.backend.web.dto.building.BuildingUpdateRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BuildingMapperTest {

    private final BuildingMapper buildingMapper = new BuildingMapper();

    @Test
    void toEntity_shouldMapFields() {
        Client owner = mock(Client.class);
        City city = mock(City.class);

        BuildingCreateRequest req = new BuildingCreateRequest(
                "Some address",
                100L,
                2000,
                BuildingType.RESIDENTIAL,
                2,
                new BigDecimal("120.50"),
                new BigDecimal("150000.00"),
                true,
                false
        );

        Building result = buildingMapper.toEntity(req, owner, city);

        assertNotNull(result);
        assertSame(owner, result.getOwner());
        assertSame(city, result.getCity());
        assertEquals("Some address", result.getAddress());
        assertEquals(2000, result.getConstructionYear());
        assertEquals(BuildingType.RESIDENTIAL, result.getBuildingType());
        assertEquals(2, result.getNumberOfFloors());
        assertEquals(new BigDecimal("120.50"), result.getSurfaceArea());
        assertEquals(new BigDecimal("150000.00"), result.getInsuredValue());
        assertTrue(result.isFloodZone());
        assertFalse(result.isEarthquakeRiskZone());
    }

    @Test
    void applyUpdate_shouldUpdateOnlyNonNullFields() {
        Client owner = mock(Client.class);
        City oldCity = mock(City.class);

        Building b = Building.builder()
                .owner(owner)
                .address("old addr")
                .city(oldCity)
                .constructionYear(1999)
                .buildingType(BuildingType.OFFICE)
                .numberOfFloors(1)
                .surfaceArea(new BigDecimal("10.00"))
                .insuredValue(new BigDecimal("1000.00"))
                .floodZone(false)
                .earthquakeRiskZone(false)
                .build();

        City newCity = mock(City.class);

        BuildingUpdateRequest req = new BuildingUpdateRequest(
                1L,
                "new addr",
                123L,
                null,
                BuildingType.RESIDENTIAL,
                null,
                new BigDecimal("50.00"),
                null,
                Boolean.TRUE,
                null
        );

        buildingMapper.applyUpdate(b, req, newCity);

        assertEquals("new addr", b.getAddress());
        assertSame(newCity, b.getCity());
        assertEquals(1999, b.getConstructionYear());
        assertEquals(BuildingType.RESIDENTIAL, b.getBuildingType());
        assertEquals(1, b.getNumberOfFloors());
        assertEquals(new BigDecimal("50.00"), b.getSurfaceArea());
        assertEquals(new BigDecimal("1000.00"), b.getInsuredValue());
        assertTrue(b.isFloodZone());
        assertFalse(b.isEarthquakeRiskZone());
    }

    @Test
    void applyUpdate_shouldNotChangeCity_whenCityParamIsNull() {
        Client owner = mock(Client.class);
        City oldCity = mock(City.class);

        Building b = Building.builder()
                .owner(owner)
                .address("addr")
                .city(oldCity)
                .constructionYear(2000)
                .buildingType(BuildingType.OFFICE)
                .numberOfFloors(2)
                .surfaceArea(new BigDecimal("10.00"))
                .insuredValue(new BigDecimal("1000.00"))
                .floodZone(false)
                .earthquakeRiskZone(true)
                .build();

        BuildingUpdateRequest req = new BuildingUpdateRequest(
                1L,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        buildingMapper.applyUpdate(b, req, null);

        assertSame(oldCity, b.getCity());
        assertEquals("addr", b.getAddress());
        assertEquals(2000, b.getConstructionYear());
        assertEquals(BuildingType.OFFICE, b.getBuildingType());
        assertEquals(2, b.getNumberOfFloors());
        assertEquals(new BigDecimal("10.00"), b.getSurfaceArea());
        assertEquals(new BigDecimal("1000.00"), b.getInsuredValue());
        assertFalse(b.isFloodZone());
        assertTrue(b.isEarthquakeRiskZone());
    }

    @Test
    void toDetails_shouldMapFieldsAndGeography() {
        Building b = mock(Building.class);

        Client owner = mock(Client.class);
        when(owner.getId()).thenReturn(10L);

        City city = mock(City.class);
        when(city.getId()).thenReturn(3L);
        when(city.getName()).thenReturn("CityName");

        County county = mock(County.class);
        when(county.getId()).thenReturn(2L);
        when(county.getName()).thenReturn("CountyName");

        Country country = mock(Country.class);
        when(country.getId()).thenReturn(1L);
        when(country.getName()).thenReturn("CountryName");

        when(city.getCounty()).thenReturn(county);
        when(county.getCountry()).thenReturn(country);

        when(b.getId()).thenReturn(999L);
        when(b.getOwner()).thenReturn(owner);
        when(b.getAddress()).thenReturn("Some address");
        when(b.getCity()).thenReturn(city);
        when(b.getConstructionYear()).thenReturn(2000);
        when(b.getBuildingType()).thenReturn(BuildingType.RESIDENTIAL);
        when(b.getNumberOfFloors()).thenReturn(2);
        when(b.getSurfaceArea()).thenReturn(new BigDecimal("120.50"));
        when(b.getInsuredValue()).thenReturn(new BigDecimal("150000.00"));
        when(b.isFloodZone()).thenReturn(true);
        when(b.isEarthquakeRiskZone()).thenReturn(false);

        BuildingDetailsResponse result = buildingMapper.toDetails(b);

        assertNotNull(result);
        assertEquals(999L, result.id());
        assertEquals(10L, result.clientId());
        assertEquals("Some address", result.address());
        assertEquals(2000, result.constructionYear());
        assertEquals(BuildingType.RESIDENTIAL, result.buildingType());
        assertEquals(2, result.numberOfFloors());
        assertEquals(new BigDecimal("120.50"), result.surfaceArea());
        assertEquals(new BigDecimal("150000.00"), result.insuredValue());
        assertTrue(result.floodZone());
        assertFalse(result.earthquakeRiskZone());
        assertNotNull(result.geography());
    }
}
