package com.insurance.backend.web.controller.broker;

import com.insurance.backend.domain.policy.PolicyStatus;
import com.insurance.backend.service.PolicyService;
import com.insurance.backend.web.dto.policy.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/brokers/policies")
public class BrokerPolicyController {

    private final PolicyService policyService;

    public BrokerPolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @GetMapping
    public Page<PolicySummaryResponse> search(@RequestParam(required = false) Long clientId,
                                              @RequestParam(required = false) Long brokerId,
                                              @RequestParam(required = false) PolicyStatus status,
                                              @RequestParam(required = false) LocalDate startDate,
                                              @RequestParam(required = false) LocalDate endDate,
                                              Pageable pageable) {
        return policyService.search(clientId, brokerId, status, startDate, endDate, pageable);
    }

    @GetMapping("/{policyId}")
    public PolicyDetailsResponse get(@PathVariable Long policyId) {
        return policyService.getById(policyId);
    }

    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public PolicyDetailsResponse createDraft(@Valid @RequestBody PolicyCreateDraftRequest request) {
        return policyService.createDraft(request);
    }

    @PostMapping("/{policyId}/activate")
    public PolicyDetailsResponse activate(@PathVariable Long policyId) {
        return policyService.activate(policyId);
    }

    @PostMapping("/{policyId}/cancel")
    public PolicyDetailsResponse cancel(@PathVariable Long policyId, @Valid @RequestBody PolicyCancelRequest request) {
        return policyService.cancel(policyId, request);
    }
}
