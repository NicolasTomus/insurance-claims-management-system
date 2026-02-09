package com.insurance.backend.web.mapper;

import com.insurance.backend.domain.metadata.currency.Currency;
import com.insurance.backend.domain.metadata.fee.FeeConfiguration;
import com.insurance.backend.domain.metadata.risk.RiskFactorConfiguration;
import com.insurance.backend.web.dto.metadata.currency.CurrencyCreateRequest;
import com.insurance.backend.web.dto.metadata.currency.CurrencyResponse;
import com.insurance.backend.web.dto.metadata.currency.CurrencyUpdateRequest;
import com.insurance.backend.web.dto.metadata.fee.FeeConfigurationCreateRequest;
import com.insurance.backend.web.dto.metadata.fee.FeeConfigurationResponse;
import com.insurance.backend.web.dto.metadata.fee.FeeConfigurationUpdateRequest;
import com.insurance.backend.web.dto.metadata.risk.RiskFactorCreateRequest;
import com.insurance.backend.web.dto.metadata.risk.RiskFactorResponse;
import com.insurance.backend.web.dto.metadata.risk.RiskFactorUpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class MetadataMapper {

    public Currency toEntity(CurrencyCreateRequest request) {
        return new Currency(
                request.code().trim().toUpperCase(),
                request.name().trim(),
                request.exchangeRateToBase(),
                request.active()
        );
    }

    public void applyUpdate(Currency currency, CurrencyUpdateRequest request) {
        if (request.name() != null) {
            currency.setName(request.name().trim());
        }
        if (request.exchangeRateToBase() != null) {
            currency.setExchangeRateToBase(request.exchangeRateToBase());
        }
        if (request.active() != null) {
            currency.setActive(request.active());
        }
    }

    public CurrencyResponse toResponse(Currency currency) {
        return new CurrencyResponse(
                currency.getId(),
                currency.getCode(),
                currency.getName(),
                currency.getExchangeRateToBase(),
                currency.isActive()
        );
    }

    public FeeConfiguration toEntity(FeeConfigurationCreateRequest request) {
        return new FeeConfiguration(
                request.name().trim(),
                request.type(),
                request.percentage(),
                request.effectiveFrom(),
                request.effectiveTo(),
                request.active()
        );
    }

    public void applyUpdate(FeeConfiguration fee, FeeConfigurationUpdateRequest request) {
        if (request.name() != null) {
            fee.setName(request.name().trim());
        }
        if (request.type() != null) {
            fee.setType(request.type());
        }
        if (request.percentage() != null) {
            fee.setPercentage(request.percentage());
        }
        if (request.effectiveFrom() != null) {
            fee.setEffectiveFrom(request.effectiveFrom());
        }
        if (request.effectiveTo() != null) {
            fee.setEffectiveTo(request.effectiveTo());
        }
        if (request.active() != null) {
            fee.setActive(request.active());
        }
    }

    public FeeConfigurationResponse toResponse(FeeConfiguration fee) {
        return new FeeConfigurationResponse(
                fee.getId(),
                fee.getName(),
                fee.getType(),
                fee.getPercentage(),
                fee.getEffectiveFrom(),
                fee.getEffectiveTo(),
                fee.isActive()
        );
    }

    public RiskFactorConfiguration toEntity(RiskFactorCreateRequest request) {
        return new RiskFactorConfiguration(
                request.level(),
                request.referenceId(),
                request.adjustmentPercentage(),
                request.active()
        );
    }

    public void applyUpdate(RiskFactorConfiguration risk, RiskFactorUpdateRequest request) {
        if (request.level() != null) {
            risk.setLevel(request.level());
        }
        if (request.referenceId() != null) {
            risk.setReferenceId(request.referenceId());
        }
        if (request.adjustmentPercentage() != null) {
            risk.setAdjustmentPercentage(request.adjustmentPercentage());
        }
        if (request.active() != null) {
            risk.setActive(request.active());
        }
    }

    public RiskFactorResponse toResponse(RiskFactorConfiguration risk) {
        return new RiskFactorResponse(
                risk.getId(),
                risk.getLevel(),
                risk.getReferenceId(),
                risk.getAdjustmentPercentage(),
                risk.isActive()
        );
    }
}
