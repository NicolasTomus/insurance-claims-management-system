package com.insurance.backend.web.controller.broker;

import com.insurance.backend.service.ClientService;
import com.insurance.backend.web.dto.client.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/brokers/clients")
public class BrokerClientController {

    private final ClientService clientService;

    public BrokerClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ClientDetailsResponse create(@Valid @RequestBody ClientCreateRequest request) {
        return clientService.create(request);
    }

    @GetMapping("/{clientId}")
    public ClientDetailsResponse get(@PathVariable Long clientId) {
        return clientService.getById(clientId);
    }

    @PutMapping("/{clientId}")
    public ClientDetailsResponse update(@PathVariable Long clientId, @Valid @RequestBody ClientUpdateRequest request) {
        return clientService.update(clientId, request);
    }

    @GetMapping
    public Page<ClientSummaryResponse> search(@RequestParam(required = false) String name,
                                              @RequestParam(required = false) String identifier,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return clientService.search(name, identifier, pageable);
    }

}
