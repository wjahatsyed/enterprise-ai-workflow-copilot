package com.wajahat.aiworkflow.action;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class JiraActionExecutor implements ActionExecutor {

    @Override
    public ActionType supportedType() {
        return ActionType.JIRA;
    }

    @Override
    public ActionExecutionResult execute(ActionStepConfig config, String inputJson) {
        return new ActionExecutionResult(
                ActionType.JIRA,
                true,
                "Jira action simulated successfully",
                Map.of(
                        "projectKey", config.parameters().getOrDefault("projectKey", "UNKNOWN"),
                        "issueType", config.parameters().getOrDefault("issueType", "Task"),
                        "input", inputJson
                )
        );
    }
}