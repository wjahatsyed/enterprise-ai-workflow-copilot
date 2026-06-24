package com.wajahat.aiworkflow.ai;

public interface OpenAiChatClient {
    String chat(String model, String systemPrompt, String context, String userQuestion);
}