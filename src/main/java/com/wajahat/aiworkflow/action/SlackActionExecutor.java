package com.wajahat.aiworkflow.action;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SlackActionExecutor implements ActionExecutor {

    @Override
    public ActionType supportedType() {
        return ActionType.SLACK;
    }

    @Override
    public ActionExecutionResult execute(ActionStepConfig config, String inputJson) {
        return new ActionExecutionResult(
                ActionType.SLACK,
                true,
                "Slack action simulated successfully",
                Map.of(
                        "channel", config.parameters().getOrDefault("channel", "unknown"),
                        "input", inputJson
                )
        );
    }
}