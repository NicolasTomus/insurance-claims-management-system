package com.insurance.backend.domain.building;

import jakarta.persistence.*;
import com.insurance.backend.domain.client.Client;
import com.insurance.backend.domain.geography.City;

import java.math.BigDecimal;

@Entity
@Table(name = "buildings")
public class Building {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_building_client"))
    private Client owner;

    @Column(nullable = false)
    private String address;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false, foreignKey = @ForeignKey(name = "fk_building_city"))
    private City city;

    @Column(nullable = false)
    private Integer constructionYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BuildingType buildingType;

    @Column(nullable = false)
    private Integer numberOfFloors;

    @Column(nullable = false)
    private BigDecimal insuredValue;

    @Column(nullable = false)
    private BigDecimal surfaceArea;

    @Column(nullable = false)
    private boolean floodZone;

    @Column(nullable = false)
    private boolean earthquakeRiskZone;

    protected Building() {}

    private Building(Builder b) {
        this.owner = b.owner;
        this.address = b.address;
        this.city = b.city;
        this.constructionYear = b.constructionYear;
        this.buildingType = b.buildingType;
        this.numberOfFloors = b.numberOfFloors;
        this.insuredValue = b.insuredValue;
        this.surfaceArea = b.surfaceArea;
        this.floodZone = b.floodZone;
        this.earthquakeRiskZone = b.earthquakeRiskZone;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Client owner;
        private String address;
        private City city;
        private Integer constructionYear;
        private BuildingType buildingType;
        private Integer numberOfFloors;
        private BigDecimal insuredValue;
        private BigDecimal surfaceArea;
        private boolean floodZone;
        private boolean earthquakeRiskZone;

        private Builder() {}

        public Builder owner(Client owner) {
            this.owner = owner;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder city(City city) {
            this.city = city;
            return this;
        }

        public Builder constructionYear(Integer constructionYear) {
            this.constructionYear = constructionYear;
            return this;
        }

        public Builder buildingType(BuildingType buildingType) {
            this.buildingType = buildingType;
            return this;
        }

        public Builder numberOfFloors(Integer numberOfFloors) {
            this.numberOfFloors = numberOfFloors;
            return this;
        }

        public Builder insuredValue(BigDecimal insuredValue) {
            this.insuredValue = insuredValue;
            return this;
        }

        public Builder surfaceArea(BigDecimal surfaceArea) {
            this.surfaceArea = surfaceArea;
            return this;
        }

        public Builder floodZone(boolean floodZone) {
            this.floodZone = floodZone;
            return this;
        }

        public Builder earthquakeRiskZone(boolean earthquakeRiskZone) {
            this.earthquakeRiskZone = earthquakeRiskZone;
            return this;
        }

        public Building build() {
            return new Building(this);
        }
    }

    public Long getId() {
        return id;
    }

    public Client getOwner() {
        return owner;
    }
    public void setOwner(Client owner) {
        this.owner = owner;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public City getCity() {
        return city;
    }
    public void setCity(City city) {
        this.city = city;
    }

    public Integer getConstructionYear() {
        return constructionYear;
    }
    public void setConstructionYear(Integer constructionYear) {
        this.constructionYear = constructionYear;
    }

    public BuildingType getBuildingType() {
        return buildingType;
    }
    public void setBuildingType(BuildingType buildingType) {
        this.buildingType = buildingType;
    }

    public Integer getNumberOfFloors() {
        return numberOfFloors;
    }
    public void setNumberOfFloors(Integer numberOfFloors) {
        this.numberOfFloors = numberOfFloors;
    }

    public BigDecimal getSurfaceArea() {
        return surfaceArea;
    }
    public void setSurfaceArea(BigDecimal surfaceArea) {
        this.surfaceArea = surfaceArea;
    }

    public BigDecimal getInsuredValue() {
        return insuredValue;
    }
    public void setInsuredValue(BigDecimal insuredValue) {
        this.insuredValue = insuredValue;
    }

    public boolean isFloodZone() {
        return floodZone;
    }
    public void setFloodZone(boolean floodZone) {
        this.floodZone = floodZone;
    }

    public boolean isEarthquakeRiskZone() {
        return earthquakeRiskZone;
    }
    public void setEarthquakeRiskZone(boolean earthquakeRiskZone) {
        this.earthquakeRiskZone = earthquakeRiskZone;
    }

}
