package com.wajahat.aiworkflow.workflow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WorkflowStepRequest(
        @Schema(example = "Manager approval")
        @NotBlank String name,
        @Schema(example = "HUMAN_APPROVAL")
        @NotNull WorkflowStepType type,
        @Schema(example = "{\"approver\":\"ops-lead@example.com\"}")
        @NotBlank String configJson
) {}
