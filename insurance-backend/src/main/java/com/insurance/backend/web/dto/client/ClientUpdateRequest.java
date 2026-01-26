package com.insurance.backend.web.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ClientUpdateRequest(
        @Size(max = 200) String name,
        @Email @Size(max = 200) String email,
        @Size(max = 40) String phone,
        @Size(max = 300) String address,
        @Size(max = 32) String identificationNumber
) {}
