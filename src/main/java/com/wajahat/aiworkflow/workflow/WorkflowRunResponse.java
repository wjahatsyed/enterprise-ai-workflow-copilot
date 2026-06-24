package com.wajahat.aiworkflow.workflow;

import java.util.List;
import java.util.UUID;

public record WorkflowRunResponse(
        UUID id,
        UUID workflowId,
        WorkflowRunStatus status,
        String inputJson,
        String outputJson,
        List<WorkflowStepRunResponse> stepRuns
) {}