package com.wajahat.aiworkflow.workspace;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants/{tenantId}/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public WorkspaceResponse create(
            @PathVariable UUID tenantId,
            @Valid @RequestBody CreateWorkspaceRequest request
    ) {
        return workspaceService.create(tenantId, request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MEMBER')")
    public List<WorkspaceResponse> findByTenant(@PathVariable UUID tenantId) {
        return workspaceService.findByTenant(tenantId);
    }
}
