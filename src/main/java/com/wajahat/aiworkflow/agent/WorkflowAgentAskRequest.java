package com.wajahat.aiworkflow.agent;

import jakarta.validation.constraints.NotBlank;

public record WorkflowAgentAskRequest(
        @NotBlank String question
) {}