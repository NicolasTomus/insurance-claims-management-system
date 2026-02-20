package com.insurance.backend.service.calculation.fee;

import com.insurance.backend.domain.metadata.fee.FeeType;
import org.springframework.stereotype.Component;

@Component
public class RiskAdjustmentFeeStrategy extends AbstractFeePercentageStrategy {

    @Override
    protected FeeType supportedType() {
        return FeeType.RISK_ADJUSTMENT;
    }

    @Override
    public int order() {
        return 30;
    }
}
