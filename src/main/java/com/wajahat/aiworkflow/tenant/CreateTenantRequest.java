package com.wajahat.aiworkflow.tenant;

import jakarta.validation.constraints.NotBlank;

public record CreateTenantRequest(
        @NotBlank String name,
        @NotBlank String slug
) {}