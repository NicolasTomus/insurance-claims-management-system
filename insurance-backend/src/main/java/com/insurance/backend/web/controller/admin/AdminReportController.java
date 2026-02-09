package com.insurance.backend.web.controller.admin;

import com.insurance.backend.service.ReportService;
import com.insurance.backend.web.dto.report.PolicyAggregateResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final ReportService reportService;

    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/policies/by-broker")
    public List<PolicyAggregateResponse> byBroker(@RequestParam(required = false) LocalDate startDate,
                                                  @RequestParam(required = false) LocalDate endDate) {
        return reportService.policiesByBroker(startDate, endDate);
    }
}
