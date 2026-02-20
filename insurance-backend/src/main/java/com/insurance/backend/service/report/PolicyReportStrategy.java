package com.insurance.backend.service.report;

import com.insurance.backend.web.dto.report.PolicyAggregateResponse;
import java.util.List;

public interface PolicyReportStrategy {
    PolicyReportType type();
    List<PolicyAggregateResponse> generate(PolicyReportFilter filter);
}