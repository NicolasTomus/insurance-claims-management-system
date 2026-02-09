package com.insurance.backend.domain.broker;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "broker")
public class Broker {

    @Id
    @GeneratedValue
    private Long id;

    private String brokerCode;

    private String name;

    private String email;

    private String phone;

    private BrokerStatus status = BrokerStatus.ACTIVE;

    private BigDecimal commissionPercentage;

    protected Broker() {}

    public Broker(String brokerCode, String name, String email, String phone, BrokerStatus status, BigDecimal commissionPercentage) {
        this.brokerCode = brokerCode;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.commissionPercentage = commissionPercentage;
    }

    public Long getId() {
        return id;
    }

    public String getBrokerCode() {
        return brokerCode;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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

    public BrokerStatus getStatus() {
        return status;
    }
    public void setStatus(BrokerStatus status) {
        this.status = status;
    }

    public BigDecimal getCommissionPercentage() {
        return commissionPercentage;
    }
    public void setCommissionPercentage(BigDecimal commissionPercentage) {
        this.commissionPercentage = commissionPercentage;
    }
}
