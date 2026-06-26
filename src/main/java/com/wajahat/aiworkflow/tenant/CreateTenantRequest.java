package com.wajahat.aiworkflow.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateTenantRequest(
        @Schema(example = "Acme Corporation")
        @NotBlank String name,
        @Schema(example = "acme")
        @NotBlank String slug
) {}
