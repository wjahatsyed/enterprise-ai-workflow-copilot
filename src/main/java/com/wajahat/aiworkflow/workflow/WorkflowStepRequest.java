package com.wajahat.aiworkflow.workflow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WorkflowStepRequest(
        @NotBlank String name,
        @NotNull WorkflowStepType type,
        @NotBlank String configJson
) {}