package com.wajahat.aiworkflow.workspace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateWorkspaceRequest(
        @Schema(example = "Support Automation")
        @NotBlank String name,
        @Schema(example = "AI workflows for support triage and escalation")
        String description
) {}
