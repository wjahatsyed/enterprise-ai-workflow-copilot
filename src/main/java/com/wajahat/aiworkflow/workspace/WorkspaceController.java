package com.wajahat.aiworkflow.workspace;

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
@RequestMapping("/api/tenants/{tenantId}/workspaces")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Workspaces", description = "Tenant workspace management")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @Operation(summary = "Create workspace", description = "Creates a workspace for a tenant. Requires TENANT_ADMIN.")
    public WorkspaceResponse create(
            @PathVariable UUID tenantId,
            @Valid @RequestBody CreateWorkspaceRequest request
    ) {
        return workspaceService.create(tenantId, request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    @Operation(summary = "List workspaces", description = "Lists workspaces for a tenant.")
    public List<WorkspaceResponse> findByTenant(@PathVariable UUID tenantId) {
        return workspaceService.findByTenant(tenantId);
    }
}
