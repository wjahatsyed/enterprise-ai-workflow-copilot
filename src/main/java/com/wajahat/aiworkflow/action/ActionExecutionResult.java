package com.wajahat.aiworkflow.action;

import java.util.Map;

public record ActionExecutionResult(
        ActionType actionType,
        boolean success,
        String message,
        Map<String, Object> metadata
) {}