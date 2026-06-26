package com.wajahat.aiworkflow.workflow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ApproveWorkflowRequest(
        @Schema(example = "ops-lead@example.com")
        @NotBlank String approvedBy
) {}
