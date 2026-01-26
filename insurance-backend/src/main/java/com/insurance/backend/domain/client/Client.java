package com.insurance.backend.domain.client;

import jakarta.persistence.*;
import com.insurance.backend.domain.building.Building;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients", uniqueConstraints = { @UniqueConstraint(name = "uk_client_identifier", columnNames = {"identificationNumber"}) })
public class Client {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientType clientType;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, updatable = false)
    private String identificationNumber;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = true)
    private String address;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Building> buildings = new ArrayList<>();

    protected Client() {}

    public Client(ClientType clientType, String name, String identificationNumber, String email, String phone, String address) {
        this.clientType = clientType;
        this.name = name;
        this.identificationNumber = identificationNumber;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public ClientType getClientType() {
        return clientType;
    }
    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

}
