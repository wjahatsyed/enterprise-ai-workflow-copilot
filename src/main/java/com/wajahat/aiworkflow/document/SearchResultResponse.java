package com.wajahat.aiworkflow.document;

import java.util.UUID;

public record SearchResultResponse(
        UUID documentId,
        UUID chunkId,
        String documentTitle,
        int chunkIndex,
        String content,
        double score
) {}