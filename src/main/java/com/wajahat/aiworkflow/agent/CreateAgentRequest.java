package com.wajahat.aiworkflow.agent;

import jakarta.validation.constraints.NotBlank;

public record CreateAgentRequest(
        @NotBlank String name,
        String description,
        @NotBlank String systemPrompt,
        String model
) {}