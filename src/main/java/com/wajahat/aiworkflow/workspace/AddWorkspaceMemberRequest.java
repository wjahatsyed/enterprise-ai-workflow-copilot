package com.wajahat.aiworkflow.workspace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddWorkspaceMemberRequest(
        @Schema(example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull UUID userId,
        @Schema(example = "MEMBER")
        @NotNull WorkspaceRole role
) {}
