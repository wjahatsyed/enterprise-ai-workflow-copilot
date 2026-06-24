package com.wajahat.aiworkflow.agent;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/api/workspaces/{workspaceId}/agents")
    public AgentResponse create(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateAgentRequest request
    ) {
        return agentService.create(workspaceId, request);
    }

    @GetMapping("/api/workspaces/{workspaceId}/agents")
    public List<AgentResponse> findByWorkspace(@PathVariable UUID workspaceId) {
        return agentService.findByWorkspace(workspaceId);
    }

    @GetMapping("/api/agents/{agentId}")
    public AgentResponse findById(@PathVariable UUID agentId) {
        return agentService.findById(agentId);
    }

    @PostMapping("/api/agents/{agentId}/ask")
    public AskAgentResponse ask(
            @PathVariable UUID agentId,
            @Valid @RequestBody AskAgentRequest request
    ) {
        return agentService.ask(agentId, request);
    }
}