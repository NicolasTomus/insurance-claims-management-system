package com.insurance.backend.web.controller.broker;

import com.insurance.backend.service.BuildingService;
import com.insurance.backend.web.dto.building.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/brokers")
public class BrokerBuildingController {

    private final BuildingService buildingService;

    public BrokerBuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @PostMapping("/clients/{clientId}/buildings")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public BuildingDetailsResponse create(@PathVariable Long clientId, @Valid @RequestBody BuildingCreateRequest request) {
        return buildingService.createForClient(clientId, request);
    }

    @GetMapping("/clients/{clientId}/buildings")
    public Page<BuildingDetailsResponse> listForClient(@PathVariable Long clientId,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return buildingService.listForClient(clientId, pageable);
    }

    @GetMapping("/buildings/{buildingId}")
    public BuildingDetailsResponse get(@PathVariable Long buildingId) {
        return buildingService.getById(buildingId);
    }

    @PutMapping("/buildings/{buildingId}")
    public BuildingDetailsResponse update(@PathVariable Long buildingId, @Valid @RequestBody BuildingUpdateRequest request) {
        return buildingService.update(buildingId, request);
    }

}
