package com.wajahat.aiworkflow.workflow;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateWorkflowRequest(
        @NotBlank String name,
        String description,
        @Valid @NotEmpty List<WorkflowStepRequest> steps
) {}