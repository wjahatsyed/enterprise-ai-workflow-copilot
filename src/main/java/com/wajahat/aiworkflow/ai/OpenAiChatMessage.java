package com.wajahat.aiworkflow.ai;

public record OpenAiChatMessage(
        String role,
        String content
) {}