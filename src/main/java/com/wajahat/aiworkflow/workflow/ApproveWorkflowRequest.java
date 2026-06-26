package com.wajahat.aiworkflow.workflow;

import jakarta.validation.constraints.NotBlank;

public record ApproveWorkflowRequest(
        @NotBlank String approvedBy
) {}