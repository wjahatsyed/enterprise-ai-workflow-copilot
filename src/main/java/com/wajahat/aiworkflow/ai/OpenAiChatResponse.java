package com.wajahat.aiworkflow.ai;

import java.util.List;

public record OpenAiChatResponse(
        List<Choice> choices
) {
    public record Choice(
            OpenAiChatMessage message
    ) {}
}