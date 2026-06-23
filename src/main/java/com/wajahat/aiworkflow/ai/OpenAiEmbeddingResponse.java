package com.wajahat.aiworkflow.ai;

import java.util.List;

public record OpenAiEmbeddingResponse(
        List<EmbeddingData> data,
        String model
) {
    public record EmbeddingData(
            List<Double> embedding,
            int index
    ) {}
}