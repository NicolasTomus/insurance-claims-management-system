package com.insurance.backend.domain.metadata.currency;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "currencies")
public class Currency {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal exchangeRateToBase;

    @Column(nullable = false)
    private boolean active = true;

    protected Currency() {}

    public Currency(String code, String name, BigDecimal exchangeRateToBase, boolean active) {
        this.code = code;
        this.name = name;
        this.exchangeRateToBase = exchangeRateToBase;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getExchangeRateToBase() {
        return exchangeRateToBase;
    }
    public void setExchangeRateToBase(BigDecimal exchangeRateToBase) {
        this.exchangeRateToBase = exchangeRateToBase;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

}
