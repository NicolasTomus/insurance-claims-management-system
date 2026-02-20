package com.insurance.backend.service.calculation.fee;

import com.insurance.backend.domain.metadata.fee.FeeType;
import org.springframework.stereotype.Component;

@Component
public class BrokerCommissionFeeStrategy extends AbstractFeePercentageStrategy {

    @Override
    protected FeeType supportedType() {
        return FeeType.BROKER_COMMISSION;
    }

    @Override
    public int order() {
        return 10;
    }
}
