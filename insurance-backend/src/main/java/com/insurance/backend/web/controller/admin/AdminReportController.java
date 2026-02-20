package com.insurance.backend.web.controller.admin;

import com.insurance.backend.domain.building.BuildingType;
import com.insurance.backend.domain.policy.PolicyStatus;
import com.insurance.backend.service.ReportService;
import com.insurance.backend.service.report.*;
import com.insurance.backend.web.dto.report.PolicyAggregateResponse;
import org.springframework.format.annotation.DateTimeFormat;
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

    @GetMapping("/policies-by-country")
    public List<PolicyAggregateResponse> byCountry(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) PolicyStatus status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) BuildingType buildingType
    ) {
        return reportService.policyReport(
                PolicyReportType.COUNTRY,
                new PolicyReportFilter(from, to, status, currency, buildingType)
        );
    }

    @GetMapping("/policies-by-county")
    public List<PolicyAggregateResponse> byCounty(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) PolicyStatus status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) BuildingType buildingType
    ) {
        return reportService.policyReport(
                PolicyReportType.COUNTY,
                new PolicyReportFilter(from, to, status, currency, buildingType)
        );
    }

    @GetMapping("/policies-by-city")
    public List<PolicyAggregateResponse> byCity(
            @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) PolicyStatus status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) BuildingType buildingType
    ) {
        return reportService.policyReport(
                PolicyReportType.CITY,
                new PolicyReportFilter(from, to, status, currency, buildingType)
        );
    }

    @GetMapping("/policies-by-broker")
    public List<PolicyAggregateResponse> byBroker(
            @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) PolicyStatus status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) BuildingType buildingType
    ) {
        return reportService.policyReport(
                PolicyReportType.BROKER,
                new PolicyReportFilter(from, to, status, currency, buildingType)
        );
    }
}