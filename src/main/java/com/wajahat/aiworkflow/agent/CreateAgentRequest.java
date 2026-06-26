package com.wajahat.aiworkflow.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateAgentRequest(
        @Schema(example = "Support Triage Agent")
        @NotBlank String name,
        @Schema(example = "Answers support workflow questions using workspace documents")
        String description,
        @Schema(example = "You are a precise support automation assistant.")
        @NotBlank String systemPrompt,
        @Schema(example = "gpt-4o-mini")
        String model
) {}
