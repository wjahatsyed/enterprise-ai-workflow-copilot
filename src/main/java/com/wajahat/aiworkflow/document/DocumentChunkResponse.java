package com.wajahat.aiworkflow.document;

import java.util.UUID;

public record DocumentChunkResponse(
        UUID id,
        UUID documentId,
        int chunkIndex,
        String content,
        int tokenEstimate
) {}