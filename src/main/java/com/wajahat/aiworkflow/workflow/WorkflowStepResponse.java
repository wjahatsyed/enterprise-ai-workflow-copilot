package com.wajahat.aiworkflow.workflow;

import java.util.UUID;

public record WorkflowStepResponse(
        UUID id,
        int stepOrder,
        String name,
        WorkflowStepType type,
        String configJson
) {}