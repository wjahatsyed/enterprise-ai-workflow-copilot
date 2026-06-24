package com.wajahat.aiworkflow.workflow;

import java.util.List;
import java.util.UUID;

public record WorkflowResponse(
        UUID id,
        UUID workspaceId,
        String name,
        String description,
        WorkflowStatus status,
        List<WorkflowStepResponse> steps
) {}