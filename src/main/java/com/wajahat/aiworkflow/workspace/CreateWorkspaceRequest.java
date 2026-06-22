package com.wajahat.aiworkflow.workspace;

import jakarta.validation.constraints.NotBlank;

public record CreateWorkspaceRequest(
        @NotBlank String name,
        String description
) {}