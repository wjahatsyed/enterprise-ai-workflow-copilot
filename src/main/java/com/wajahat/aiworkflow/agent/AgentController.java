package com.wajahat.aiworkflow.agent;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/api/workspaces/{workspaceId}/agents")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public AgentResponse create(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateAgentRequest request
    ) {
        return agentService.create(workspaceId, request);
    }

    @GetMapping("/api/workspaces/{workspaceId}/agents")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    public List<AgentResponse> findByWorkspace(@PathVariable UUID workspaceId) {
        return agentService.findByWorkspace(workspaceId);
    }

    @GetMapping("/api/agents/{agentId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    public AgentResponse findById(@PathVariable UUID agentId) {
        return agentService.findById(agentId);
    }

    @PostMapping("/api/agents/{agentId}/ask")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    public AskAgentResponse ask(
            @PathVariable UUID agentId,
            @Valid @RequestBody AskAgentRequest request
    ) {
        return agentService.ask(agentId, request);
    }
}
