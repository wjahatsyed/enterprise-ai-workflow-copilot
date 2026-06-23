package com.wajahat.aiworkflow.ai;

import java.util.List;

public interface OpenAiEmbeddingClient {
    List<Double> embed(String input);
}