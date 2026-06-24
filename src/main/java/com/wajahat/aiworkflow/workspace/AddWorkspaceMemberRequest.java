package com.wajahat.aiworkflow.workspace;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddWorkspaceMemberRequest(
        @NotNull UUID userId,
        @NotNull WorkspaceRole role
) {}