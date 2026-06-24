package com.wajahat.aiworkflow.ai;

import java.util.List;

public record OpenAiChatRequest(
        String model,
        List<OpenAiChatMessage> messages,
        double temperature
) {}