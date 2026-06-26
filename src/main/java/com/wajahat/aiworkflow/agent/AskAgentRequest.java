package com.wajahat.aiworkflow.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;

public record AskAgentRequest(
        @Schema(example = "123e4567-e89b-12d3-a456-426614174000")
        UUID conversationId,
        @Schema(example = "Which workflow should handle SLA escalations?")
        @NotBlank String question
) {}
