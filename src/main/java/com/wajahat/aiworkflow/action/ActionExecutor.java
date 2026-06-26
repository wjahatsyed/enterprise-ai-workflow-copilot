package com.wajahat.aiworkflow.action;

public interface ActionExecutor {

    ActionType supportedType();

    ActionExecutionResult execute(ActionStepConfig config, String inputJson);
}