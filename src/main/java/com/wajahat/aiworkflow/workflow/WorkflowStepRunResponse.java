package com.wajahat.aiworkflow.workflow;

import java.time.LocalDateTime;
import java.util.UUID;

public record WorkflowStepRunResponse(
        UUID id,
        UUID workflowStepId,
        String stepName,
        WorkflowStepType stepType,
        WorkflowRunStatus status,
        String inputJson,
        String outputJson,
        String errorMessage,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {}