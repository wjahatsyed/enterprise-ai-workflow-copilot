package com.wajahat.aiworkflow.workspace;

import java.util.UUID;

public record WorkspaceResponse(
        UUID id,
        UUID tenantId,
        String name,
        String description
) {}