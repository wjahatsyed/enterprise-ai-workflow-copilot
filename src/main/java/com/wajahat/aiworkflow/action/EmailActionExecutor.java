package com.wajahat.aiworkflow.action;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class EmailActionExecutor implements ActionExecutor {

    @Override
    public ActionType supportedType() {
        return ActionType.EMAIL;
    }

    @Override
    public ActionExecutionResult execute(ActionStepConfig config, String inputJson) {
        return new ActionExecutionResult(
                ActionType.EMAIL,
                true,
                "Email action simulated successfully",
                Map.of(
                        "to", config.parameters().getOrDefault("to", "unknown"),
                        "subject", config.parameters().getOrDefault("subject", "No subject"),
                        "input", inputJson
                )
        );
    }
}