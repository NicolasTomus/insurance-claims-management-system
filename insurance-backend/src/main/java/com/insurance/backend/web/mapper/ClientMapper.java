package com.insurance.backend.web.mapper;

import com.insurance.backend.domain.client.Client;
import com.insurance.backend.web.dto.client.ClientCreateRequest;
import com.insurance.backend.web.dto.client.ClientDetailsResponse;
import com.insurance.backend.web.dto.client.ClientSummaryResponse;
import com.insurance.backend.web.dto.client.ClientUpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    private final BuildingMapper buildingMapper;

    public ClientMapper(BuildingMapper buildingMapper) {
        this.buildingMapper = buildingMapper;
    }

    public Client toEntity(ClientCreateRequest req) {
        return new Client(
                req.clientType(),
                req.name().trim(),
                req.identificationNumber().trim(),
                req.email(),
                req.phone(),
                req.address()
        );
    }

    public void applyUpdate(Client client, ClientUpdateRequest req) {
        if (req.name() != null) client.setName(req.name().trim());
        if (req.email() != null) client.setEmail(req.email());
        if (req.phone() != null) client.setPhone(req.phone());
        if (req.address() != null) client.setAddress(req.address());
    }

    public ClientSummaryResponse toSummary(Client c) {
        return new ClientSummaryResponse(
                c.getId(),
                c.getClientType(),
                c.getName(),
                c.getIdentificationNumber()
        );
    }

    public ClientDetailsResponse toDetails(Client c) {
        return new ClientDetailsResponse(
                c.getId(),
                c.getClientType(),
                c.getName(),
                c.getIdentificationNumber(),
                c.getEmail(),
                c.getPhone(),
                c.getAddress(),
                c.getBuildings().stream().map(buildingMapper::toDetails).toList()
        );
    }
}
