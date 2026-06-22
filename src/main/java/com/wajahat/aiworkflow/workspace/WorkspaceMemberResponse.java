package com.wajahat.aiworkflow.workspace;

import java.util.UUID;

public record WorkspaceMemberResponse(
        UUID id,
        UUID workspaceId,
        UUID userId,
        String fullName,
        String email,
        WorkspaceRole role
) {}