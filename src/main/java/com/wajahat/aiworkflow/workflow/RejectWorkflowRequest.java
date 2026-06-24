package com.wajahat.aiworkflow.workflow;

import jakarta.validation.constraints.NotBlank;

public record RejectWorkflowRequest(
        @NotBlank String rejectedBy,
        @NotBlank String reason
) {}