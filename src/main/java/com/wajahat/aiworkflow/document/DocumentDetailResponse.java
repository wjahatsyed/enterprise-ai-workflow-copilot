package com.wajahat.aiworkflow.document;

import java.util.List;
import java.util.UUID;

public record DocumentDetailResponse(
        UUID id,
        UUID workspaceId,
        String title,
        DocumentSourceType sourceType,
        DocumentStatus status,
        List<DocumentChunkResponse> chunks
) {}