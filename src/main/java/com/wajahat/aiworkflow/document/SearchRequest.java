package com.wajahat.aiworkflow.document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record SearchRequest(
        @Schema(example = "How should high priority tickets be escalated?")
        @NotBlank String query,
        @Schema(example = "5")
        Integer topK
) {}
