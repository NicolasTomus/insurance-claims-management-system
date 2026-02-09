package com.insurance.backend.web.dto.policy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PolicyCancelRequest(
        @NotBlank @Size(max = 500) String reason
) {
}
