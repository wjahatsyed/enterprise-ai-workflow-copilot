package com.wajahat.aiworkflow.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "wajahat@example.com")
        @Email @NotBlank String email
) {
}
