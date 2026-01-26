package com.insurance.backend.web.dto.client;

import com.insurance.backend.domain.client.ClientType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClientCreateRequest(
        @NotNull ClientType clientType,
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 32) String identificationNumber,
        @Email @Size(max = 200) String email,
        @Size(max = 40) String phone,
        @Size(max = 300) String address
) {}
