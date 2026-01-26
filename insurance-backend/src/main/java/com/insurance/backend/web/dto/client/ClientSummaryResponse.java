package com.insurance.backend.web.dto.client;

import com.insurance.backend.domain.client.ClientType;

public record ClientSummaryResponse(
        Long id,
        ClientType clientType,
        String name,
        String identificationNumber
) {}
