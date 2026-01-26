package com.insurance.backend.domain.geography;

import jakarta.persistence.*;
import com.insurance.backend.domain.building.Building;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cities", uniqueConstraints = @UniqueConstraint(name = "uk_city_county", columnNames = {"county_id", "name"}))
public class City {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "county_id", nullable = false, foreignKey = @ForeignKey(name = "fk_city_county"))
    private County county;

    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Building> buildings = new ArrayList<>();

    protected City() {}

    public City(String name, County county) {
        this.name = name;
        this.county = county;
    }

    public Long getId () {
        return id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public County getCounty() {
        return county;
    }
    public void setCounty(County county) {
        this.county = county;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

}
