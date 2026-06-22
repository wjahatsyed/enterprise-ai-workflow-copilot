package com.wajahat.aiworkflow.document;

import java.util.UUID;

public record DocumentResponse(
        UUID id,
        UUID workspaceId,
        String title,
        DocumentSourceType sourceType,
        DocumentStatus status,
        int chunkCount
) {}