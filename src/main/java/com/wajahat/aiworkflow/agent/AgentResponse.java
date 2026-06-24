package com.wajahat.aiworkflow.agent;

import java.util.UUID;

public record AgentResponse(
        UUID id,
        UUID workspaceId,
        String name,
        String description,
        String model,
        AgentStatus status
) {}