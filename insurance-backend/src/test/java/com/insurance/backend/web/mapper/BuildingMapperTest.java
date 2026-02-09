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

    private static final long OWNER_ID = 10L;
    private static final long CITY_ID = 3L;
    private static final long COUNTY_ID = 2L;
    private static final long COUNTRY_ID = 1L;
    private static final long BUILDING_ID = 999L;

    private static final long UPDATE_ID = 1L;
    private static final long CITY_ID_RAW = 100L;
    private static final long CITY_ID_UPDATE = 123L;

    private static final String ADDRESS_SOME = "Some address";
    private static final String ADDRESS_OLD = "old addr";
    private static final String ADDRESS_NEW = "new addr";
    private static final String ADDRESS = "addr";

    private static final String CITY_NAME = "CityName";
    private static final String COUNTY_NAME = "CountyName";
    private static final String COUNTRY_NAME = "CountryName";

    private static final int YEAR_2000 = 2000;
    private static final int YEAR_1999 = 1999;
    private static final int FLOORS_2 = 2;
    private static final int FLOORS_1 = 1;

    private static final BigDecimal SURFACE_120_50 = new BigDecimal("120.50");
    private static final BigDecimal SURFACE_50_00 = new BigDecimal("50.00");
    private static final BigDecimal SURFACE_10_00 = new BigDecimal("10.00");

    private static final BigDecimal INSURED_150000_00 = new BigDecimal("150000.00");
    private static final BigDecimal INSURED_1000_00 = new BigDecimal("1000.00");

    private static final boolean FLOOD_TRUE = true;
    private static final boolean FLOOD_FALSE = false;

    private static final boolean EARTHQUAKE_TRUE = true;
    private static final boolean EARTHQUAKE_FALSE = false;

    private final BuildingMapper buildingMapper = new BuildingMapper();

    @Test
    void toEntityShouldMapFields() {
        Client owner = mock(Client.class);
        City city = mock(City.class);

        BuildingCreateRequest req = new BuildingCreateRequest(
                ADDRESS_SOME,
                CITY_ID_RAW,
                YEAR_2000,
                BuildingType.RESIDENTIAL,
                FLOORS_2,
                SURFACE_120_50,
                INSURED_150000_00,
                FLOOD_TRUE,
                EARTHQUAKE_FALSE
        );

        Building result = buildingMapper.toEntity(req, owner, city);

        assertNotNull(result);
        assertSame(owner, result.getOwner());
        assertSame(city, result.getCity());
        assertEquals(ADDRESS_SOME, result.getAddress());
        assertEquals(YEAR_2000, result.getConstructionYear());
        assertEquals(BuildingType.RESIDENTIAL, result.getBuildingType());
        assertEquals(FLOORS_2, result.getNumberOfFloors());
        assertEquals(SURFACE_120_50, result.getSurfaceArea());
        assertEquals(INSURED_150000_00, result.getInsuredValue());
        assertTrue(result.isFloodZone());
        assertFalse(result.isEarthquakeRiskZone());
    }

    @Test
    void applyUpdateShouldUpdateOnlyNonNullFields() {
        Client owner = mock(Client.class);
        City oldCity = mock(City.class);

        Building building = Building.builder()
                .owner(owner)
                .address(ADDRESS_OLD)
                .city(oldCity)
                .constructionYear(YEAR_1999)
                .buildingType(BuildingType.OFFICE)
                .numberOfFloors(FLOORS_1)
                .surfaceArea(SURFACE_10_00)
                .insuredValue(INSURED_1000_00)
                .floodZone(FLOOD_FALSE)
                .earthquakeRiskZone(EARTHQUAKE_FALSE)
                .build();

        City newCity = mock(City.class);

        BuildingUpdateRequest req = new BuildingUpdateRequest(
                UPDATE_ID,
                ADDRESS_NEW,
                CITY_ID_UPDATE,
                null,
                BuildingType.RESIDENTIAL,
                null,
                SURFACE_50_00,
                null,
                Boolean.TRUE,
                null
        );

        buildingMapper.applyUpdate(building, req, newCity);

        assertEquals(ADDRESS_NEW, building.getAddress());
        assertSame(newCity, building.getCity());
        assertEquals(YEAR_1999, building.getConstructionYear());
        assertEquals(BuildingType.RESIDENTIAL, building.getBuildingType());
        assertEquals(FLOORS_1, building.getNumberOfFloors());
        assertEquals(SURFACE_50_00, building.getSurfaceArea());
        assertEquals(INSURED_1000_00, building.getInsuredValue());
        assertTrue(building.isFloodZone());
        assertFalse(building.isEarthquakeRiskZone());
    }

    @Test
    void applyUpdateShouldNotChangeCityWhenCityParamIsNull() {
        Client owner = mock(Client.class);
        City oldCity = mock(City.class);

        Building building = Building.builder()
                .owner(owner)
                .address(ADDRESS)
                .city(oldCity)
                .constructionYear(YEAR_2000)
                .buildingType(BuildingType.OFFICE)
                .numberOfFloors(FLOORS_2)
                .surfaceArea(SURFACE_10_00)
                .insuredValue(INSURED_1000_00)
                .floodZone(FLOOD_FALSE)
                .earthquakeRiskZone(EARTHQUAKE_TRUE)
                .build();

        BuildingUpdateRequest req = new BuildingUpdateRequest(
                UPDATE_ID,
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

        buildingMapper.applyUpdate(building, req, null);

        assertSame(oldCity, building.getCity());
        assertEquals(ADDRESS, building.getAddress());
        assertEquals(YEAR_2000, building.getConstructionYear());
        assertEquals(BuildingType.OFFICE, building.getBuildingType());
        assertEquals(FLOORS_2, building.getNumberOfFloors());
        assertEquals(SURFACE_10_00, building.getSurfaceArea());
        assertEquals(INSURED_1000_00, building.getInsuredValue());
        assertFalse(building.isFloodZone());
        assertTrue(building.isEarthquakeRiskZone());
    }

    @Test
    void toDetailsShouldMapFieldsAndGeography() {
        Building building = mock(Building.class);

        Client owner = mock(Client.class);
        when(owner.getId()).thenReturn(OWNER_ID);

        City city = mock(City.class);
        when(city.getId()).thenReturn(CITY_ID);
        when(city.getName()).thenReturn(CITY_NAME);

        County county = mock(County.class);
        when(county.getId()).thenReturn(COUNTY_ID);
        when(county.getName()).thenReturn(COUNTY_NAME);

        Country country = mock(Country.class);
        when(country.getId()).thenReturn(COUNTRY_ID);
        when(country.getName()).thenReturn(COUNTRY_NAME);

        when(city.getCounty()).thenReturn(county);
        when(county.getCountry()).thenReturn(country);

        when(building.getId()).thenReturn(BUILDING_ID);
        when(building.getOwner()).thenReturn(owner);
        when(building.getAddress()).thenReturn(ADDRESS_SOME);
        when(building.getCity()).thenReturn(city);
        when(building.getConstructionYear()).thenReturn(YEAR_2000);
        when(building.getBuildingType()).thenReturn(BuildingType.RESIDENTIAL);
        when(building.getNumberOfFloors()).thenReturn(FLOORS_2);
        when(building.getSurfaceArea()).thenReturn(SURFACE_120_50);
        when(building.getInsuredValue()).thenReturn(INSURED_150000_00);
        when(building.isFloodZone()).thenReturn(FLOOD_TRUE);
        when(building.isEarthquakeRiskZone()).thenReturn(EARTHQUAKE_FALSE);

        BuildingDetailsResponse result = buildingMapper.toDetails(building);

        assertNotNull(result);
        assertEquals(BUILDING_ID, result.id());
        assertEquals(OWNER_ID, result.clientId());
        assertEquals(ADDRESS_SOME, result.address());
        assertEquals(YEAR_2000, result.constructionYear());
        assertEquals(BuildingType.RESIDENTIAL, result.buildingType());
        assertEquals(FLOORS_2, result.numberOfFloors());
        assertEquals(SURFACE_120_50, result.surfaceArea());
        assertEquals(INSURED_150000_00, result.insuredValue());
        assertTrue(result.floodZone());
        assertFalse(result.earthquakeRiskZone());
        assertNotNull(result.geography());
    }
}
