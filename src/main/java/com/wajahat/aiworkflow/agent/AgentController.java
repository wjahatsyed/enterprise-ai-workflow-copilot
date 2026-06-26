package com.wajahat.aiworkflow.agent;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Agents", description = "AI agent management and chat")
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/api/workspaces/{workspaceId}/agents")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @Operation(summary = "Create agent", description = "Creates an AI agent in a workspace. Requires TENANT_ADMIN.")
    public AgentResponse create(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateAgentRequest request
    ) {
        return agentService.create(workspaceId, request);
    }

    @GetMapping("/api/workspaces/{workspaceId}/agents")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    @Operation(summary = "List workspace agents", description = "Lists AI agents in a workspace.")
    public List<AgentResponse> findByWorkspace(@PathVariable UUID workspaceId) {
        return agentService.findByWorkspace(workspaceId);
    }

    @GetMapping("/api/agents/{agentId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    @Operation(summary = "Get agent", description = "Returns a single AI agent.")
    public AgentResponse findById(@PathVariable UUID agentId) {
        return agentService.findById(agentId);
    }

    @PostMapping("/api/agents/{agentId}/ask")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    @Operation(summary = "Ask agent", description = "Sends a question to an AI agent and records the conversation.")
    public AskAgentResponse ask(
            @PathVariable UUID agentId,
            @Valid @RequestBody AskAgentRequest request
    ) {
        return agentService.ask(agentId, request);
    }
}
