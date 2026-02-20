package com.insurance.backend.service.calculation.fee;

import com.insurance.backend.domain.metadata.fee.FeeType;
import org.springframework.stereotype.Component;

@Component
public class AdminFeeStrategy extends AbstractFeePercentageStrategy {

    @Override
    protected FeeType supportedType() {
        return FeeType.ADMIN_FEE;
    }

    @Override
    public int order() {
        return 20;
    }
}
