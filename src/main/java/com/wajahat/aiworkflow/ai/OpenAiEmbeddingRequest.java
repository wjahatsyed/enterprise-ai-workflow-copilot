package com.wajahat.aiworkflow.ai;

public record OpenAiEmbeddingRequest(
        String model,
        String input,
        String encoding_format
) {}