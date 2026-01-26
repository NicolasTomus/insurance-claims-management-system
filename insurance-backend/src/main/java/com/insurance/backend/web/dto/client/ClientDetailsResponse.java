package com.insurance.backend.web.dto.client;

import com.insurance.backend.domain.client.ClientType;
import com.insurance.backend.web.dto.building.BuildingDetailsResponse;

import java.util.List;

public record ClientDetailsResponse(
        Long id,
        ClientType clientType,
        String name,
        String identificationNumber,
        String email,
        String phone,
        String address,
        List<BuildingDetailsResponse> buildings
) {}
