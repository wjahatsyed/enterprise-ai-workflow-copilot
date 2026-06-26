package com.wajahat.aiworkflow.action;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class WebhookActionExecutor implements ActionExecutor {

    @Override
    public ActionType supportedType() {
        return ActionType.WEBHOOK;
    }

    @Override
    public ActionExecutionResult execute(ActionStepConfig config, String inputJson) {
        return new ActionExecutionResult(
                ActionType.WEBHOOK,
                true,
                "Webhook action simulated successfully",
                Map.of(
                        "url", config.parameters().getOrDefault("url", "unknown"),
                        "input", inputJson
                )
        );
    }
}