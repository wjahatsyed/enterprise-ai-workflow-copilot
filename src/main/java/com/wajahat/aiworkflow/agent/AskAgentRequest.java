package com.wajahat.aiworkflow.agent;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;

public record AskAgentRequest(
        UUID conversationId,
        @NotBlank String question
) {}