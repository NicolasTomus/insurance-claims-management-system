package com.insurance.backend.web.controller.admin;

import com.insurance.backend.service.FeeConfigurationService;
import com.insurance.backend.web.dto.metadata.fee.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/fees")
public class AdminFeeConfigurationController {

    private final FeeConfigurationService feeConfigurationService;

    public AdminFeeConfigurationController(FeeConfigurationService feeConfigurationService) {
        this.feeConfigurationService = feeConfigurationService;
    }

    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public FeeConfigurationResponse create(@Valid @RequestBody FeeConfigurationCreateRequest request) {
        return feeConfigurationService.create(request);
    }

    @GetMapping("/{feeId}")
    public FeeConfigurationResponse get(@PathVariable Long feeId) {
        return feeConfigurationService.getById(feeId);
    }

    @PutMapping("/{feeId}")
    public FeeConfigurationResponse update(@PathVariable Long feeId, @Valid @RequestBody FeeConfigurationUpdateRequest request) {
        return feeConfigurationService.update(feeId, request);
    }

    @GetMapping
    public Page<FeeConfigurationResponse> list(Pageable pageable) {
        return feeConfigurationService.list(pageable);
    }
}
