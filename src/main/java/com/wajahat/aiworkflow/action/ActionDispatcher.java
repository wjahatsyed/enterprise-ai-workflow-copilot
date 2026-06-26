package com.wajahat.aiworkflow.action;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ActionDispatcher {

    private final Map<ActionType, ActionExecutor> executors = new EnumMap<>(ActionType.class);

    public ActionDispatcher(List<ActionExecutor> executorList) {
        for (ActionExecutor executor : executorList) {
            executors.put(executor.supportedType(), executor);
        }
    }

    public ActionExecutionResult execute(ActionStepConfig config, String inputJson) {
        ActionExecutor executor = executors.get(config.actionType());

        if (executor == null) {
            throw new IllegalArgumentException("No executor found for action type: " + config.actionType());
        }

        return executor.execute(config, inputJson);
    }
}