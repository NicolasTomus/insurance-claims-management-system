package com.insurance.backend.web.controller.admin;

import com.insurance.backend.service.BrokerService;
import com.insurance.backend.web.dto.broker.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/brokers")
public class AdminBrokerController {

    private final BrokerService brokerService;

    public AdminBrokerController(BrokerService brokerService) {
        this.brokerService = brokerService;
    }

    @GetMapping
    public Page<BrokerDetailsResponse> search(@RequestParam(required = false) String name,
                                              @RequestParam(required = false) String brokerCode,
                                              Pageable pageable) {
        return brokerService.search(name, brokerCode, pageable);
    }

    @GetMapping("/{brokerId}")
    public BrokerDetailsResponse get(@PathVariable Long brokerId) {
        return brokerService.getById(brokerId);
    }

    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public BrokerDetailsResponse create(@Valid @RequestBody BrokerCreateRequest request) {
        return brokerService.create(request);
    }

    @PutMapping("/{brokerId}")
    public BrokerDetailsResponse update(@PathVariable Long brokerId, @Valid @RequestBody BrokerUpdateRequest request) {
        return brokerService.update(brokerId, request);
    }

    @PostMapping("/{brokerId}/activate")
    public BrokerDetailsResponse activate(@PathVariable Long brokerId) {
        return brokerService.activate(brokerId);
    }

    @PostMapping("/{brokerId}/deactivate")
    public BrokerDetailsResponse deactivate(@PathVariable Long brokerId) {
        return brokerService.deactivate(brokerId);
    }
}
