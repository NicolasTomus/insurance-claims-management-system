package com.insurance.backend.web.controller.admin;

import com.insurance.backend.service.RiskFactorService;
import com.insurance.backend.web.dto.metadata.risk.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/risk-factors")
public class AdminRiskFactorController {

    private final RiskFactorService riskFactorService;

    public AdminRiskFactorController(RiskFactorService riskFactorService) {
        this.riskFactorService = riskFactorService;
    }

    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public RiskFactorResponse create(@Valid @RequestBody RiskFactorCreateRequest request) {
        return riskFactorService.create(request);
    }

    @GetMapping("/{riskFactorId}")
    public RiskFactorResponse get(@PathVariable Long riskFactorId) {
        return riskFactorService.getById(riskFactorId);
    }

    @PutMapping("/{riskFactorId}")
    public RiskFactorResponse update(@PathVariable Long riskFactorId, @Valid @RequestBody RiskFactorUpdateRequest request) {
        return riskFactorService.update(riskFactorId, request);
    }

    @GetMapping
    public Page<RiskFactorResponse> list(Pageable pageable) {
        return riskFactorService.list(pageable);
    }
}
