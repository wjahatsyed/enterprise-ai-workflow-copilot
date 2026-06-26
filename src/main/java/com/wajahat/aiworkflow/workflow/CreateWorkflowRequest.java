package com.wajahat.aiworkflow.workflow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateWorkflowRequest(
        @Schema(example = "Approval-backed escalation")
        @NotBlank String name,
        @Schema(example = "Routes high priority cases through approval and external action steps")
        String description,
        @Valid @NotEmpty List<WorkflowStepRequest> steps
) {}
