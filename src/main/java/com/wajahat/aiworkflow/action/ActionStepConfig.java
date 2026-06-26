package com.wajahat.aiworkflow.action;

import java.util.Map;

public record ActionStepConfig(
        ActionType actionType,
        Map<String, Object> parameters
) {}