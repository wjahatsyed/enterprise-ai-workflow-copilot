package com.wajahat.aiworkflow.workflow;

import java.util.UUID;

public record AiAgentStepConfig(
        UUID agentId,
        String promptTemplate
) {}