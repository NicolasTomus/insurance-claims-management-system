package com.insurance.backend.domain.geography;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "countries", uniqueConstraints = @UniqueConstraint(name = "uk_country_name", columnNames = "name"))
public class Country {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<County> counties = new ArrayList<>();

    protected Country() {}

    public Country(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<County> getCounties() {
        return counties;
    }



}
