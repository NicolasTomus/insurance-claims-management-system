package com.insurance.backend.domain.metadata.risk;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "risk_factor_configurations")
public class RiskFactorConfiguration {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel level;

    @Column(nullable = true)
    private Long referenceId;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal adjustmentPercentage;

    @Column(nullable = false)
    private boolean active = true;

    protected RiskFactorConfiguration() {}

    public RiskFactorConfiguration(RiskLevel level, Long referenceId, BigDecimal adjustmentPercentage, boolean active) {
        this.level = level;
        this.referenceId = referenceId;
        this.adjustmentPercentage = adjustmentPercentage;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public RiskLevel getLevel() {
        return level;
    }
    public void setLevel(RiskLevel level) {
        this.level = level;
    }

    public Long getReferenceId() {
        return referenceId;
    }
    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public BigDecimal getAdjustmentPercentage() {
        return adjustmentPercentage;
    }
    public void setAdjustmentPercentage(BigDecimal adjustmentPercentage) {
        this.adjustmentPercentage = adjustmentPercentage;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

}
