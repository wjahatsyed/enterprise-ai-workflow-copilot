package com.wajahat.aiworkflow.agent;

import java.util.UUID;

public record AskAgentResponse(
        UUID conversationId,
        String answer
) {}