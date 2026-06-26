package com.wajahat.aiworkflow.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @Schema(example = "Wajahat Syed")
        @NotBlank String fullName,
        @Schema(example = "wajahat@example.com")
        @Email @NotBlank String email,
        @Schema(example = "TENANT_ADMIN")
        @NotNull UserRole role
) {}
