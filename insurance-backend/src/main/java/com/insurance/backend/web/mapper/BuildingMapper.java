package com.insurance.backend.web.mapper;

import com.insurance.backend.domain.building.Building;
import com.insurance.backend.domain.client.Client;
import com.insurance.backend.domain.geography.City;
import com.insurance.backend.domain.geography.County;
import com.insurance.backend.domain.geography.Country;
import com.insurance.backend.web.dto.building.*;
import org.springframework.stereotype.Component;

@Component
public class BuildingMapper {

    public Building toEntity(BuildingCreateRequest req, Client owner, City city) {
        return Building.builder()
                .owner(owner)
                .address(req.address())
                .city(city)
                .constructionYear(req.constructionYear())
                .buildingType(req.buildingType())
                .numberOfFloors(req.numberOfFloors())
                .insuredValue(req.insuredValue())
                .surfaceArea(req.surfaceArea())
                .floodZone(req.floodZone())
                .earthquakeRiskZone(req.earthquakeRiskZone())
                .build();
    }

    public void applyUpdate(Building b, BuildingUpdateRequest req, City maybeNewCity) {
        if (req.address() != null) b.setAddress(req.address());
        if (maybeNewCity != null) b.setCity(maybeNewCity);
        if (req.constructionYear() != null) b.setConstructionYear(req.constructionYear());
        if (req.buildingType() != null) b.setBuildingType(req.buildingType());
        if (req.numberOfFloors() != null) b.setNumberOfFloors(req.numberOfFloors());
        if (req.surfaceArea() != null) b.setSurfaceArea(req.surfaceArea());
        if (req.insuredValue() != null) b.setInsuredValue(req.insuredValue());
        if (req.floodZone() != null) b.setFloodZone(req.floodZone());
        if (req.earthquakeRiskZone() != null) b.setEarthquakeRiskZone(req.earthquakeRiskZone());
    }

    public BuildingDetailsResponse toDetails(Building b) {
        City city = b.getCity();
        County county = city.getCounty();
        Country country = county.getCountry();

        return new BuildingDetailsResponse(
                b.getId(),
                b.getOwner().getId(),
                b.getAddress(),
                b.getConstructionYear(),
                b.getBuildingType(),
                b.getNumberOfFloors(),
                b.getSurfaceArea(),
                b.getInsuredValue(),
                b.isFloodZone(),
                b.isEarthquakeRiskZone(),
                new GeographyBrief(
                        country.getId(), country.getName(), county.getId(),
                        county.getName(), city.getId(), city.getName()
                )
        );
    }
}
